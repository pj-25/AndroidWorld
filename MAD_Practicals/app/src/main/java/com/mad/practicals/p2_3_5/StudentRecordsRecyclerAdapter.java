package com.mad.practicals.p2_3_5;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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
        img.setImageDrawable(studentRecord.getImage());
        holder.studentInfoCardView.findViewById(R.id.delete_action_btn).setOnClickListener(v -> {
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
