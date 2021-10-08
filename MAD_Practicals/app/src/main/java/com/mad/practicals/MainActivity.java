package com.mad.practicals;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private int gifChoice = 0;
    private boolean wasCollapsed;

    private static final int []gifResources = {R.raw.android_logo1, R.raw.android_logo2, R.raw.android_logo3, R.raw.android_logo4, R.raw.android_logo5, R.raw.android_logo6};
    public static ArrayList<PracticalInfo> practicalInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadAppBarGIF();
        ListView practicalListView = findViewById(R.id.practical_list_view);

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

        loadPracticalInfo();
        practicalListView.setAdapter(new PracticalListAdapter(this, R.layout.practical_list_row_layout, practicalInfos));
        practicalListView.setOnItemClickListener((parent, view, position, id) -> {
            startPracticalOverview(position, view);
        });
    }

    void loadPracticalInfo(){
        String[] aims = getResources().getStringArray(R.array.practical_aims);
        String[] launchers = getResources().getStringArray(R.array.practical_launchers);
        TypedArray imgIds = getResources().obtainTypedArray(R.array.practical_img_ids);
        practicalInfos = new ArrayList<>(launchers.length);
        for(int i=0;i<launchers.length;i++){
            practicalInfos.add(new PracticalInfo(i, "Practical: "+(i+1), aims[i], launchers[i], imgIds.getResourceId(i, 0)));
        }
        imgIds.recycle();
        Log.d("DATA", practicalInfos.toString());
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
        }else if(selectedItemId == R.id.github_link){
            jumpToGitHub();
        }
        return super.onOptionsItemSelected(item);
    }


    private void jumpToUrl(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void jumpToGitHub() {
        jumpToUrl("https://github.com/pj-25/AndroidWorld");
    }

    public void jumpToTeams(){
        jumpToUrl("https://teams.microsoft.com/l/channel/19%3a1VW6-vZbvP7tcCVMTlz1YFYfo6zI-atm_XDdW4LP3yg1%40thread.tacv2/General?groupId=ec3e36a3-f1f4-4e0e-8b55-ecc34cf7a2e2&tenantId=c8bb283e-4f46-4285-aafc-e97e0952ebd0");
    }

    public void startPracticalOverview(int id, View view){
        Intent intent = new Intent(this, PracticalOverviewActivity.class);
        intent.putExtra(PracticalOverviewActivity.PRACTICAL_ID, id);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view.findViewById(R.id.practical_row_img), "practical_img");
        startActivity(intent, options.toBundle());
    }

    public void startPracticalOverview(int id){
        Intent intent = new Intent(this, PracticalOverviewActivity.class);
        intent.putExtra(PracticalOverviewActivity.PRACTICAL_ID, id);
        startActivity(intent);
    }

    public static class PracticalListAdapter extends ArrayAdapter<PracticalInfo>{

        private int rowLayout;

        public PracticalListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PracticalInfo> data) {
            super(context, resource, data);
            rowLayout = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            @SuppressLint("ViewHolder") View rowView = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
            ImageView practicalImg = rowView.findViewById(R.id.practical_row_img);
            practicalImg.setImageResource(getItem(position).getImgId());
            TextView practicalLabel = rowView.findViewById(R.id.practical_row_label);
            practicalLabel.setText(getItem(position).getLabel());
            return rowView;
        }

        public static class ViewHolder {
            private ImageView practicalImg;
            private TextView practicalLabel;
        }
    }
}