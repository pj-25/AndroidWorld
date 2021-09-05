package com.mad.practicals.p2_3_5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mad.practicals.R;
import com.mad.practicals.p2_3_5.remoteDataLoader.RemoteDataLoader;
import com.mad.practicals.p2_3_5.retrofitConnection.RetrofitConnection;
import com.mad.practicals.p2_3_5.retrofitConnection.StudentRecordsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentRecordsActivity extends AppCompatActivity {

    private static final String REMOTE_BASE_URL = "http://studentrecords.us-east-2.elasticbeanstalk.com";
    private static final String FETCH_ALL_REQUEST = "/fetch_all.php";

    private static LinkedList<StudentRecord> studentRecords = null;
    private static boolean isLocalDataLoaded = false;

    private TextView title;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private LoadRemoteDataTask loadRemoteDataTask;

    private static int currentTheme = R.style.Theme_MAD_Practicals;
    private static final int []themeIds = { R.style.Theme_MAD_Practicals, R.style.Theme_Charcoal, R.style.Theme_Crayola, R.style.Theme_PrussianBlue};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_records);

        setSupportActionBar(findViewById(R.id.student_records_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = findViewById(R.id.student_list_title);

        recyclerView = findViewById(R.id.student_recycler_view);

        RecyclerView.LayoutManager layoutManager;
        int orientation = getResources().getConfiguration().orientation;
        layoutManager = (orientation == Configuration.ORIENTATION_PORTRAIT)? new LinearLayoutManager(this):new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
    }


    @Override
    protected void onStart() {
        promptFetchOptions();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_records_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.theme_option){
            promptThemes();
        }else if(item.getItemId() == R.id.menuitem_fetch){
            promptFetchOptions();
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void promptThemes(){
        AlertDialog.Builder themePrompt = new AlertDialog.Builder(this);
        themePrompt.setTitle("Select Theme");
        final String []themeOptions = { "Default", "Charcoal", "Crayola", "Prussian Blue", "Cancel"};
        themePrompt.setItems(themeOptions, (dialog, which) -> {
            if(which==themeOptions.length-1){
                dialog.dismiss();
            }
            else{
                changeTheme(which);
            }
        });
        themePrompt.show();
    }

    public void promptFetchOptions(){
        AlertDialog.Builder fetchPrompt = new AlertDialog.Builder(this);
        fetchPrompt.setTitle("Select fetch option");
        final String []fetchOptions = {"Local", "Remote[Async Task]", "Remote[Retrofit]", "Cancel"};
        fetchPrompt.setItems(fetchOptions, (dialog, which) -> {
            switch (which){
                case 0:
                    fetchLocalData();
                    break;
                case 1:
                    fetchRemoteDataUsingAsyncTask();
                    break;
                case 2:
                    fetchRemoteDataUsingRetrofit();
                    break;
                default:
                    dialog.dismiss();
            }
        });
        fetchPrompt.show();
    }
    
    public void changeTheme(int themeCode){
        currentTheme = themeIds[themeCode];
        recreate();
    }

    public void updateTitle(int totalRecords, String type){
        String text = "Student Records("+ totalRecords +") - "+type;
        title.setText(text);
    }

    public void fetchLocalData() {
        if(studentRecords == null || !isLocalDataLoaded){
            studentRecords = new LinkedList<>();
            String []nameArray = getResources().getStringArray(R.array.student_name_list);
            String []addressArray = getResources().getStringArray(R.array.student_address_list);
            TypedArray imageArray = getResources().obtainTypedArray(R.array.student_img_list);
            for(int i=0;i<nameArray.length;i++){
                studentRecords.addLast(new StudentRecord(nameArray[i], addressArray[i], imageArray.getDrawable(i)));
            }
            imageArray.recycle();
            updateTitle(studentRecords.size(), "Local");
            recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords, pos -> {
                updateTitle(studentRecords.size()-1, "Local");
            }));
            isLocalDataLoaded = true;
        }
    }

    public void fetchRemoteDataUsingAsyncTask(){
        loadRemoteDataTask = new LoadRemoteDataTask();
        loadRemoteDataTask.execute(REMOTE_BASE_URL, FETCH_ALL_REQUEST);
    }

    public void fetchRemoteDataUsingRetrofit(){
        Call<LinkedList<StudentRecord>> call = RetrofitConnection.getInstance().getStudentRecordsApi().fetchAll();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Remote Data Fetch [Retrofit]");
        progressDialog.show();
        call.enqueue(new Callback<LinkedList<StudentRecord>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<LinkedList<StudentRecord>> call, Response<LinkedList<StudentRecord>> response) {
                LinkedList<StudentRecord> newStudentRecords = response.body();
                if(newStudentRecords!=null) {
                    for(StudentRecord studentRecord: newStudentRecords){
                        Glide.with(StudentRecordsActivity.this)
                                .load(StudentRecordsApi.BASE_URL+"/"+studentRecord.getImagePath())
                                .into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        studentRecord.setImage(resource);
                                        if(studentRecord == newStudentRecords.getLast()){
                                            studentRecords = newStudentRecords;
                                            updateTitle(studentRecords.size(), "Remote [Retrofit]");
                                            recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords, pos -> {
                                                updateTitle(studentRecords.size()-1, "Remote [Retrofit]");
                                            }));
                                            isLocalDataLoaded = false;
                                            progressDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                    }
                }
                else{
                    displayError();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<LinkedList<StudentRecord>> call, Throwable t) {
                displayError();
                progressDialog.dismiss();
            }

            public void displayError(){
                Toast.makeText(StudentRecordsActivity.this, "No Internet Connection :(\nUnable to fetch data using retrofit :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class LoadRemoteDataTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            if (progressDialog == null || !progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(StudentRecordsActivity.this, "Remote Data Fetch [Async Task]", "Loading...", true);
            }
        }
        @Override
        protected JSONArray doInBackground(String... url) {
            try {
                RemoteDataLoader remoteDataLoader = new RemoteDataLoader();
                JSONArray jsonArray = remoteDataLoader.getDataInJSON(new URL(url[0]+"/"+url[1]));
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String imagePath = jsonObject.getString("img_path");
                    jsonObject.put("image", remoteDataLoader.getDrawable(new URL(url[0]+"/"+imagePath)));
                }
                return jsonArray;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            if(jsonArray!=null){
                LinkedList<StudentRecord> newStudentRecords = new LinkedList<>();
                try {
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        newStudentRecords.addLast(new StudentRecord(jsonObject.getString("name"), jsonObject.getString("address"), (Drawable)jsonObject.get("image")));
                    }
                    studentRecords = newStudentRecords;
                    updateTitle(studentRecords.size(), "Remote [Async Task]");
                    recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords, pos -> {
                        updateTitle(studentRecords.size()-1, "Remote [Async Task]");
                    }));
                    isLocalDataLoaded = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(StudentRecordsActivity.this, "No Internet Connection :( \nUnable to load data using async task", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
            loadRemoteDataTask.cancel(true);
        }
        isLocalDataLoaded = false;
        super.onStop();
    }
}