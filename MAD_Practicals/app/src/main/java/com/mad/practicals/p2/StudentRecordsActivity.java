package com.mad.practicals.p2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mad.practicals.R;

import java.util.LinkedList;

public class StudentRecordsActivity extends AppCompatActivity {

    private static LinkedList<StudentRecord> studentRecords = null;

    private TextView title;

    private static int currentTheme = R.style.Theme_MAD_Practicals;
    private static final int []themeIds = { R.style.Theme_MAD_Practicals, R.style.Theme_Charcoal, R.style.Theme_Crayola, R.style.Theme_PrussianBlue};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_records);

        setSupportActionBar(findViewById(R.id.student_records_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadData();

        title = findViewById(R.id.student_list_title);
        updateTitle(studentRecords.size());

        RecyclerView recyclerView = findViewById(R.id.student_recycler_view);
        recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords, pos -> {
            updateTitle(studentRecords.size()-1);
        }));
        RecyclerView.LayoutManager layoutManager;
        int orientation = getResources().getConfiguration().orientation;
        layoutManager = (orientation == Configuration.ORIENTATION_PORTRAIT)? new LinearLayoutManager(this):new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_theme, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.theme_option){
            promptThemes();
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

    public void changeTheme(int themeCode){
        currentTheme = themeIds[themeCode];
        recreate();
    }

    public void updateTitle(int totalRecords){
        String text = "Student Records("+ totalRecords +")";
        title.setText(text);
    }

    public void loadData() {
        if(studentRecords == null){
            studentRecords = new LinkedList<>();
            String []nameArray = getResources().getStringArray(R.array.student_name_list);
            String []addressArray = getResources().getStringArray(R.array.student_address_list);
            TypedArray imageArray = getResources().obtainTypedArray(R.array.student_img_list);
            for(int i=0;i<nameArray.length;i++){
                studentRecords.addLast(new StudentRecord(nameArray[i], addressArray[i], imageArray.getDrawable(i)));
            }
            imageArray.recycle();
        }
    }
}