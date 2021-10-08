package com.mad.practicals.p2_3_5_6;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.practicals.R;
import com.mad.practicals.p2_3_5_6.remoteDataLoader.RemoteDataLoader;
import com.mad.practicals.p2_3_5_6.retrofitConnection.RetrofitConnection;
import com.mad.practicals.p2_3_5_6.retrofitConnection.StudentRecordsApi;

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

    private static final String THEME_PREFERENCE = "THEME_PREF";
    private static final String THEME_CODE_PREF = "THEME_CODE";

    private static final String REMOTE_BASE_URL = "http://studentrecordsapi.us-east-2.elasticbeanstalk.com";
    private static final String FETCH_ALL_REQUEST = "/fetch_all.php";

    private static LinkedList<StudentRecord> studentRecords = null;
    private static boolean isLocalDataLoaded = false;

    private TextView title;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private LoadRemoteDataTask loadRemoteDataTask;
    private FloatingActionButton addRecordsFab;
    private DatabaseReference studentDB;
    private ChildEventListener childEventListener;

    private static int fetchType = -1;
    private static final int LOCAL_FETCH = 0;
    private static final int REMOTE_FETCH_ASYNC = 1;
    private static final int REMOTE_FETCH_RETROFIT = 2;
    private static final int REMOTE_FETCH_FIREBASE = 3;

    private static boolean themeChanged = false;
    private static int currentThemeCode;
    private static final int []themeIds = { R.style.Theme_MAD_Practicals, R.style.Theme_Charcoal, R.style.Theme_Crayola, R.style.Theme_PrussianBlue};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!themeChanged){
            currentThemeCode = getSharedPreferences(THEME_PREFERENCE, MODE_PRIVATE).getInt(THEME_CODE_PREF, 0);
        }
        setTheme(themeIds[currentThemeCode]);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_records);

        setSupportActionBar(findViewById(R.id.student_records_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = findViewById(R.id.student_list_title);
        recyclerView = findViewById(R.id.student_recycler_view);
        addRecordsFab = ((FloatingActionButton)findViewById(R.id.add_records_fab));
        addRecordsFab.hide();
        addRecordsFab.setOnClickListener(v -> {
            createStudentRecords();
        });

        RecyclerView.LayoutManager layoutManager;
        int orientation = getResources().getConfiguration().orientation;
        layoutManager = (orientation == Configuration.ORIENTATION_PORTRAIT)? new LinearLayoutManager(this):new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        progressDialog = new ProgressDialog(this);

        if(savedInstanceState!=null){
            fetchType = savedInstanceState.getInt("FETCH_TYPE");
            if(studentRecords==null){
                fetchData(fetchType);
            }else{
                loadDataIntoRecyclerView();
            }
        }
    }


    @Override
    protected void onStart() {
        if(fetchType==-1) {
            promptFetchOptions();
            themeChanged = false;
        }
        super.onStart();
    }

    public void createStudentRecords(){
        Intent srInputIntent = new Intent(this, StudentRecordInputActivity.class);
        startActivity(srInputIntent);
    }

    public void fetchData(int fetchType){
        switch(fetchType){
            case LOCAL_FETCH:
                fetchLocalData();
                break;
            case REMOTE_FETCH_ASYNC:
                fetchRemoteDataUsingAsyncTask();
                break;
            case REMOTE_FETCH_RETROFIT:
                fetchRemoteDataUsingRetrofit();
                break;
            case REMOTE_FETCH_FIREBASE:
                fetchDataFromFirebase();
                break;
        }
    }

    public void loadDataIntoRecyclerView(){
        if(studentRecords!=null){
            recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords, pos->updateTitle(studentRecords.size()-1)));
        }
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
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Save Preferences");
        themePrompt.setView(checkBox);
        themePrompt.setSingleChoiceItems(themeOptions,currentThemeCode, (dialog, which) -> {
            if( which != themeOptions.length-1 && checkBox.isChecked()){
                saveThemePreferences(which);
            }
            dialog.dismiss();
            changeTheme(which);
        });

        themePrompt.show();
    }

    public void saveThemePreferences(int themeCode){
        SharedPreferences sharedPreferences = getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THEME_CODE_PREF, themeCode);
        editor.apply();
    }

    public void promptFetchOptions(){
        AlertDialog.Builder fetchPrompt = new AlertDialog.Builder(this);
        fetchPrompt.setTitle("Select fetch option");
        final String []fetchOptions = {"Local", "Remote [Async Task]", "Remote [Retrofit]", "Remote [Firebase]", "Cancel"};
        fetchPrompt.setItems(fetchOptions, (dialog, which) -> {
            if(which>3){
                dialog.dismiss();
            }else{
                fetchData(which);
            }
        });
        fetchPrompt.show();
    }
    
    public void changeTheme(int themeCode){
        currentThemeCode = themeCode;
        themeChanged = true;
        recreate();
    }

    public void updateTitle(int totalRecords, String type){
        String text = "Student Records("+ totalRecords +") - "+type;
        title.setText(text);
    }

    public void updateTitle(int totalRecords){
        String type ="";
        switch (fetchType){
            case LOCAL_FETCH:
                type= "Local";
                break;
            case REMOTE_FETCH_ASYNC:
                type="Remote [Async]";
                break;
            case REMOTE_FETCH_RETROFIT:
                type = "Remote [Retrofit]";
                break;
            case REMOTE_FETCH_FIREBASE:
                type= "Remote [Firebase]";
        }
        updateTitle(totalRecords, type);
    }

    public void fetchLocalData() {
        fetchType = LOCAL_FETCH;
        addRecordsFab.hide();
        if(studentRecords == null || !isLocalDataLoaded){
            studentRecords = new LinkedList<>();
            String []nameArray = getResources().getStringArray(R.array.student_name_list);
            String []addressArray = getResources().getStringArray(R.array.student_address_list);
            TypedArray imageArray = getResources().obtainTypedArray(R.array.student_img_list);
            for(int i=0;i<nameArray.length;i++){
                studentRecords.addLast(new StudentRecord(nameArray[i], addressArray[i], imageArray.getDrawable(i)));
            }
            imageArray.recycle();
            loadDataIntoRecyclerView();
            isLocalDataLoaded = true;
        }
    }

    public void fetchDataFromFirebase(){
        if(fetchType == REMOTE_FETCH_FIREBASE && recyclerView.getAdapter()!=null){
            return;
        }
        addRecordsFab.show();
        fetchType = REMOTE_FETCH_FIREBASE;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Fetching Remote Data [Firebase]");
        progressDialog.setMessage("Please wait!");
        progressDialog.show();

        studentRecords = new LinkedList<>();
        recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords, pos -> {
            updateTitle(studentRecords.size()-1, "Remote [Firebase]");
            studentDB.child(studentRecords.get(pos).getKey()).removeValue();
        }));
        isLocalDataLoaded = false;
        StudentRecordsRecyclerAdapter adapter = (StudentRecordsRecyclerAdapter)recyclerView.getAdapter();
        if(studentDB == null){
            studentDB = FirebaseDatabase.getInstance().getReference("student");
        }
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                StudentRecord studentRecord = snapshot.getValue(StudentRecord.class);
                studentRecord.setKey(snapshot.getKey());
                studentRecords.addFirst(studentRecord);
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
                updateTitle(studentRecords.size(), "Remote[Firebase]");
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                StudentRecord studentRecord = snapshot.getValue(StudentRecord.class);
                studentRecord.setKey(snapshot.getKey());
                updateStudentRecord(studentRecord);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                deleteStudentRecord(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        studentDB.addChildEventListener(childEventListener);
    }

    public void updateStudentRecord(StudentRecord updatedRecord){
        int i=0;
        for(StudentRecord studentRecord:studentRecords){
            if(studentRecord.getKey().equals(updatedRecord.getKey())){
                studentRecords.set(i, updatedRecord);
                recyclerView.getAdapter().notifyItemChanged(i);
                break;
            }
            i++;
        }
    }

    public void deleteStudentRecord(String key){
        int i=0;
        for(StudentRecord studentRecord: studentRecords){
            if(studentRecord.getKey().equals(key)){
                studentRecords.remove(i);
                recyclerView.getAdapter().notifyItemRemoved(i);
                updateTitle(studentRecords.size(), "Remote[Firebase]");
                FirebaseStorage.getInstance().getReferenceFromUrl(studentRecord.getImagePath()).delete();
                break;
            }
            i++;
        }
    }

    public void fetchRemoteDataUsingAsyncTask(){
        fetchType = REMOTE_FETCH_ASYNC;
        addRecordsFab.hide();
        loadRemoteDataTask = new LoadRemoteDataTask();
        loadRemoteDataTask.execute(REMOTE_BASE_URL, FETCH_ALL_REQUEST);
    }

    public void fetchRemoteDataUsingRetrofit(){
        fetchType = REMOTE_FETCH_RETROFIT;
        addRecordsFab.hide();
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
                        studentRecord.setImagePath(StudentRecordsApi.BASE_URL+"/"+studentRecord.getImagePath());
                        /*
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
                         */
                        studentRecords = newStudentRecords;
                        updateTitle(studentRecords.size(), "Remote [Retrofit]");
                        recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords, pos -> {
                            updateTitle(studentRecords.size()-1, "Remote [Retrofit]");
                        }));
                        isLocalDataLoaded = false;
                        progressDialog.dismiss();
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
                Toast.makeText(StudentRecordsActivity.this, "Server Down / Please check Internet Connection :(\nUnable to fetch data using retrofit :(", Toast.LENGTH_SHORT).show();
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
                    jsonObject.put("img_path", url[0]+"/"+imagePath);
                    //jsonObject.put("image", remoteDataLoader.getDrawable(new URL(url[0]+"/"+imagePath)));
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
                        //newStudentRecords.addLast(new StudentRecord(jsonObject.getString("name"), jsonObject.getString("address"), (Drawable)jsonObject.get("image")));
                        newStudentRecords.addLast(new StudentRecord(jsonObject.getString("name"), jsonObject.getString("address"), jsonObject.getString("img_path")));
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
                Toast.makeText(StudentRecordsActivity.this, "Server down / Please check Internet Connection :( \nUnable to load data using async task", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if(fetchType!=-1){
            outState.putInt("FETCH_TYPE", fetchType);
        }
        super.onSaveInstanceState(outState);
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

    @Override
    protected void onDestroy() {
        if(studentDB!=null && childEventListener!=null)
            studentDB.removeEventListener(childEventListener);
        super.onDestroy();
    }
}