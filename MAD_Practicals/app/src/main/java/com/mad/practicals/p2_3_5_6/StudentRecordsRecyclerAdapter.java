package com.mad.practicals.p2_3_5_6;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mad.practicals.R;

import java.util.LinkedList;

public class StudentRecordsRecyclerAdapter extends RecyclerView.Adapter<StudentRecordsRecyclerAdapter.StudentInfoViewHolder> {

    private LinkedList<StudentRecord> studentRecords;
    private OnDeleteListener onDeleteListener;

    public StudentRecordsRecyclerAdapter(LinkedList<StudentRecord> studentRecords, OnDeleteListener onDeleteListener){
        this.studentRecords = studentRecords;
        this.onDeleteListener = onDeleteListener;
    }

    public StudentRecordsRecyclerAdapter(LinkedList<StudentRecord> studentRecords){
        this(studentRecords, null);
    }

    @NonNull
    @Override
    public StudentInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StudentInfoViewHolder((CardView)LayoutInflater.from(parent.getContext()).inflate(R.layout.student_record_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StudentInfoViewHolder holder, int position) {
        TextView name = holder.studentInfoCardView.findViewById(R.id.student_name);
        TextView address = holder.studentInfoCardView.findViewById(R.id.student_address);
        ImageView img = holder.studentInfoCardView.findViewById(R.id.student_img);
        StudentRecord studentRecord = studentRecords.get(position);
        name.setText(studentRecord.getName());
        address.setText(studentRecord.getAddress());
        ImageView editActionBtn = holder.studentInfoCardView.findViewById(R.id.edit_record_btn);

        if(studentRecord.getImage()!=null){
            img.setImageDrawable(studentRecord.getImage());
            editActionBtn.setVisibility(View.INVISIBLE);

        }else{
            Glide.with(img.getContext()).load(studentRecord.getImagePath()).placeholder(R.drawable.loading).into(img);
            if(studentRecord.getKey()!=null){
                editActionBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(editActionBtn.getContext(), StudentRecordInputActivity.class);
                    intent.putExtra("KEY", studentRecord.getKey());
                    intent.putExtra("STUDENT_NAME", studentRecord.getName());
                    intent.putExtra("STUDENT_ADDRESS", studentRecord.getAddress());
                    intent.putExtra("IMAGE_PATH", studentRecord.getImagePath());
                    editActionBtn.getContext().startActivity(intent);
                });
            }else{
                editActionBtn.setVisibility(View.INVISIBLE);
            }
        }
        holder.studentInfoCardView.findViewById(R.id.delete_record_btn).setOnClickListener(v -> {
            if(onDeleteListener!=null){
                onDeleteListener.onDelete(holder.getAdapterPosition());
            }
            studentRecords.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
        });
    }

    public LinkedList<StudentRecord> getStudentRecords() {
        return studentRecords;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setStudentRecords(LinkedList<StudentRecord> studentRecords) {
        this.studentRecords = studentRecords;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return studentRecords.size();
    }

    static class StudentInfoViewHolder extends RecyclerView.ViewHolder{

        private CardView studentInfoCardView;

        public StudentInfoViewHolder(@NonNull CardView itemView) {
            super(itemView);
            studentInfoCardView = itemView;
        }

        public CardView getStudentInfoCardView() {
            return studentInfoCardView;
        }

        public void setStudentInfoCardView(CardView studentInfoCardView) {
            this.studentInfoCardView = studentInfoCardView;
        }
    }

    public interface OnDeleteListener{
        void onDelete(int pos);
    }

}
