package com.mad.practicals.p2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.TypedArray;
import android.os.Bundle;

import com.mad.practicals.R;

import java.util.LinkedList;

public class StudentRecordsActivity extends AppCompatActivity {

    private static LinkedList<StudentRecord> studentRecords = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_records);

        loadData();

        RecyclerView recyclerView = findViewById(R.id.student_recycler_view);
        recyclerView.setAdapter(new StudentRecordsRecyclerAdapter(studentRecords));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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