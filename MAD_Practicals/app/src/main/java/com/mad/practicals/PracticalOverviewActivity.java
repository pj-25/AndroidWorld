package com.mad.practicals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mad.practicals.p2_3_5_6.StudentRecordsActivity;
import com.mad.practicals.p7.BatteryStatusActivity;

public class PracticalOverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_overview);
    }

    public void launch(View view){
        Intent intent = new Intent(this, BatteryStatusActivity.class);
        startActivity(intent);
    }
}