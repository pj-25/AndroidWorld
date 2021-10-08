package com.mad.practicals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PracticalOverviewActivity extends AppCompatActivity {

    public static final String PRACTICAL_ID = "pid";
    private int pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_overview);

        if(getIntent()!=null){
            pid = getIntent().getExtras().getInt(PRACTICAL_ID);
            PracticalInfo practicalInfo = MainActivity.practicalInfos.get(pid);
            setTitle(practicalInfo.getLabel());
            ImageView pimg = findViewById(R.id.practical_image);
            pimg.setImageResource(practicalInfo.getImgId());
            TextView paim = findViewById(R.id.practical_aim_content);
            paim.setText(practicalInfo.getAim());
            ((Button)findViewById(R.id.launch_btn)).setOnClickListener(v -> {
                onLaunch();
            });
        }else{
            Toast.makeText(this, "Error in fetching practical :(", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLaunch(){
        Intent launchIntent;
        String launcher = MainActivity.practicalInfos.get(pid).getLauncherActivity();
        if(pid == 3 || pid == 8 ){
            launchIntent = getPackageManager().getLaunchIntentForPackage(launcher);
            if(launchIntent==null){
                String msg = "Unable to find ";
                if(pid == 3){
                    msg += "Blogholic App";
                }else{
                    msg += "Chichat App";
                }
                msg+="\nPlease contact Joshi Prashant" +
                        "\njoshiprashant.jp25@gmail.com";
                Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
        }else{
            launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(this, launcher));
        }
        launchIntent.putExtra(PRACTICAL_ID, pid);
        startActivity(launchIntent);
    }
}