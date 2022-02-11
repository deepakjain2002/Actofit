package io.deepakjain.actofit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText etname, etnum;
    Button next;
    SharedPreferences sharedPreferences;

    public static final String SHARED_PREF_NAME = "mypref";
    public static final String KEY_NAME = "name";
    public static final String KEY_NUM = "num";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etname = findViewById(R.id.editTextTextPersonName);
        etnum = findViewById(R.id.editTextPhone);
        next = findViewById(R.id.button);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE);

        String name = sharedPreferences.getString(KEY_NAME,null);
        if(name != null){
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_NAME,etname.getText().toString());
            editor.putString(KEY_NUM,etnum.getText().toString());
            editor.apply();

            Intent intent = new Intent(MainActivity.this,HomeActivity.class);
            startActivity(intent);

            Toast.makeText(MainActivity.this,"Details Captured", Toast.LENGTH_SHORT).show();
            }
        });
    }
}