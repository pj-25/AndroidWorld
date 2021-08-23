package com.mad.practicals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private int gifChoice = 0;
    private boolean wasCollapsed;
    private static final int []gifResources = {R.raw.android_logo1, R.raw.android_logo2, R.raw.android_logo3, R.raw.android_logo4, R.raw.android_logo5, R.raw.android_logo6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadAppBarGIF();

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if(Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()){
                if(wasCollapsed){
                    loadAppBarGIF();
                }
            }else{
                wasCollapsed = true;
            }
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            startPracticalOverview();
        });
    }

    void loadAppBarGIF(){
        Glide.with(this).load(gifResources[gifChoice]).fitCenter().into((ImageView)findViewById(R.id.app_bar_image));
        gifChoice = (gifChoice+1) % gifResources.length;
        wasCollapsed = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedItemId = item.getItemId();
        if (selectedItemId == R.id.teams_mad_link) {
            jumpToTeams();
        }
        return super.onOptionsItemSelected(item);
    }

    public void jumpToTeams(){
        Intent teamsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://teams.microsoft.com/l/channel/19%3a1VW6-vZbvP7tcCVMTlz1YFYfo6zI-atm_XDdW4LP3yg1%40thread.tacv2/General?groupId=ec3e36a3-f1f4-4e0e-8b55-ecc34cf7a2e2&tenantId=c8bb283e-4f46-4285-aafc-e97e0952ebd0"));
        startActivity(teamsIntent);
    }

    public void startPracticalOverview(){
        Intent intent = new Intent(this, PracticalOverviewActivity.class);
        startActivity(intent);
    }
}