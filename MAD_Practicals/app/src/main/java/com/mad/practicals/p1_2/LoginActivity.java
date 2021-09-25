package com.mad.practicals.p1_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.mad.practicals.R;

public class LoginActivity extends AppCompatActivity {
    private EditText emailIdField;
    private EditText passwordField;
    public static String LABEL = LoginActivity.class.getSimpleName();

    private static int currentLayoutChoice = R.id.menu_relative;
    private static int currentLayout = R.layout.activity_login_relative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(currentLayout);

        setSupportActionBar(findViewById(R.id.login_toolbar));

        Glide.with(this).load(R.raw.login).into((ImageView)findViewById(R.id.login_gif));

        emailIdField = findViewById(R.id.emailId);
        passwordField = findViewById(R.id.password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_layout_menu, menu);
        Log.d(LABEL, currentLayoutChoice+"");
        menu.findItem(currentLayoutChoice).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        currentLayoutChoice = item.getItemId();
        if(currentLayoutChoice == R.id.menu_relative){
            currentLayout = R.layout.activity_login_relative;
        }
        else if(currentLayoutChoice == R.id.menu_linear){
            currentLayout = R.layout.activity_login_linear;
        }
        else{
            currentLayout = R.layout.activity_login_table;
        }
        recreate();
        return super.onOptionsItemSelected(item);
    }

    public void showPassword(View view){
        CheckBox showCheckBox = (CheckBox)view;
        if(showCheckBox.isChecked()){
            passwordField.setTransformationMethod(null);
        }else{
            passwordField.setTransformationMethod(new PasswordTransformationMethod());
        }
    }

    public void login(View view){
        String emailId, password;
        emailId = emailIdField.getText().toString().trim();
        password = passwordField.getText().toString().trim();
        if(emailId.isEmpty()){
            emailIdField.setError("Please enter email Id!");
            emailIdField.requestFocus();
        }else if(password.isEmpty()){
            passwordField.setError("Please enter password!");
            passwordField.requestFocus();
        }else{
            Snackbar.make(view, "Login successfully :)", Snackbar.LENGTH_LONG).show();
            Intent sendIntent = new Intent(this, SenderActivity.class);     //Explicit intent
            startActivity(sendIntent);
        }
    }

    @Override
    protected void onStart() {
        Log.d(LABEL, "......Started");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(LABEL, "......Resumed");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(LABEL, "......Paused");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(LABEL, "......Stopped");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LABEL, "......Destroyed");
        super.onDestroy();
    }
}