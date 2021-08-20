package com.mad.practicals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mad.practicals.p1.LoginActivity;

public class PracticalOverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_overview);
    }

    public void launch(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}