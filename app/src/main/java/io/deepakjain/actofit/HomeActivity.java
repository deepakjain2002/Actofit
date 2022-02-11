package io.deepakjain.actofit;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    TextView textViewName, textViewNum, textViewLoc;
    Button logout,location;
    BackgroundServices backgroundServices=null;
    Boolean mBound = false;
    String data;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BackgroundServices.LocalBinder binder = (BackgroundServices.LocalBinder)iBinder;
            backgroundServices = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            backgroundServices = null;
            mBound = false;
        }
    };

    SharedPreferences sharedPreferences;

    public static final String SHARED_PREF_NAME = "mypref";
    public static final String KEY_NAME = "name";
    public static final String KEY_NUM = "num";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        location = findViewById(R.id.location);
                        location.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                backgroundServices.requestLocationUpdates();
                            }
                        });

                        setButtonState(Common.requestingLocationUpdates(HomeActivity.this));
                        bindService(new Intent(HomeActivity.this, BackgroundServices.class),
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();

        textViewName = findViewById(R.id.tvName);
        textViewNum = findViewById(R.id.tvNum);
        logout = findViewById(R.id.logout);
        textViewLoc = findViewById(R.id.tvLoc);


        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE);

        String name = sharedPreferences.getString(KEY_NAME,null);
        String contact = sharedPreferences.getString(KEY_NUM,null);

        if(name != null || contact != null){
            textViewName.setText(name);
            textViewNum.setText(contact);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                Toast.makeText(HomeActivity.this,"Logging Out", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onStop() {
        if(mBound) {
            unbindService(mServiceConnection);
            mBound=false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(Common.KEY_REQUESTING_LOCATION_UPDATES))
        {
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTING_LOCATION_UPDATES,false));
        }
    }

    private void setButtonState(boolean isRequestEnable) {
        if(isRequestEnable)
        {
            location.setEnabled(true);
        }
        else
        {
            location.setEnabled(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event)
    {
        if(event != null ){
             data = new StringBuilder()
                    .append(event.getLocation().getLatitude())
                    .append("/")
                    .append(event.getLocation().getLongitude())
                    .toString();

            textViewLoc.setText(data);
            writeFile();

        }
    }

    private void writeFile() {
        try{
            FileOutputStream fileOutputStream = openFileOutput("DataStored.txt",MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();

            Toast.makeText(getApplicationContext(), "Data Stored in internal Storage" + getFilesDir(), Toast.LENGTH_SHORT).show();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}