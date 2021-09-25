package com.mad.practicals.p7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad.practicals.BuildConfig;
import com.mad.practicals.R;

public class BatteryStatusActivity extends AppCompatActivity {

    private ImageView batteryStatusImg;
    private TextView batteryPercentView;
    private BroadcastReceiver batteryStatusReceiver;
    private float batteryPercentValue;
    private boolean isPowerConnected;

    private final static String NOTIFICATION_CHANNEL = "BATTERY_STATUS_"+BuildConfig.APPLICATION_ID;
    private final static int NOTIFICATION_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_status);

        batteryStatusImg = findViewById(R.id.battery_status_img);
        batteryPercentView = findViewById(R.id.battery_percentage);

        batteryStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action){
                    case Intent.ACTION_BATTERY_CHANGED:
                        int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,  -1);
                        float batteryPercentValue = batteryLevel*100/ (float)scale;
                        batteryChanged(batteryPercentValue);
                        break;
                    case Intent.ACTION_POWER_CONNECTED:
                        powerConnected();
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED:
                        powerDisconnected();
                        break;
                }
            }
        };

        registerBatteryStatusReceiver();
        createNotificationChannel();
    }

    public void showNotification(String msg){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_battery)
                .setContentTitle("MAD Practical - Battery Status")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, "Battery Status", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notifies battery status");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void registerBatteryStatusReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryStatusReceiver, intentFilter);
    }

    public void unregisterBatteryStatusReceiver(){
        unregisterReceiver(batteryStatusReceiver);
    }

    public void powerConnected(){
        isPowerConnected = true;
        setBatteryImage();
        showNotification("Power connected");
    }

    public void powerDisconnected(){
        isPowerConnected = false;
        setBatteryImage();
        showNotification("Power disconnected");
    }

    public void batteryChanged(float batteryPercentage){
        String btext = batteryPercentage+" %";
        batteryPercentView.setText(btext);
        batteryPercentValue = batteryPercentage;
        if(!isPowerConnected)
            setBatteryImage();
    }

    public void setBatteryImage(){
        if(isPowerConnected){
            batteryStatusImg.setImageResource(R.drawable.power_on);
            batteryPercentView.setTextColor(Color.YELLOW);
        }
        else if(batteryPercentValue <= 15.0){
            batteryStatusImg.setImageResource(R.drawable.low_battery);
            batteryPercentView.setTextColor(Color.RED);
        }else if(batteryPercentValue < 75.0){
            batteryStatusImg.setImageResource(R.drawable.medium_battery);
            batteryPercentView.setTextColor(Color.BLUE);
        }else{
            batteryStatusImg.setImageResource(R.drawable.high_battery);
            batteryPercentView.setTextColor(Color.GREEN);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterBatteryStatusReceiver();
        super.onDestroy();
    }
}