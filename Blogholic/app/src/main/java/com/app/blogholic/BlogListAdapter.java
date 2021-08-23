package com.app.blogholic;


import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.blogholic.databaseHandler.BlogholicSQLiteHelper;

import java.util.LinkedList;

public class BlogListAdapter extends RecyclerView.Adapter<BlogListAdapter.BlogCardViewHolder> {

    private LinkedList<BlogCardInfo> blogCardInfoList;
    private BlogholicSQLiteHelper blogholicSQLiteHelper;
    private BlogCardClickListener blogCardClickListener;

    private static final String[] BLOG_CARD_DATA_COLUMNS = new String[]{BlogholicSQLiteHelper.BlogTable.TITLE, BlogholicSQLiteHelper.BlogTable.ENTRY_DATE, BlogholicSQLiteHelper.BlogTable.IMG_RES_PATH};


    public BlogListAdapter(LinkedList<BlogCardInfo> blogCardInfoList, BlogholicSQLiteHelper blogholicSQLiteHelper, BlogCardClickListener blogCardClickListener){
        this.blogCardInfoList = blogCardInfoList;
        this.blogholicSQLiteHelper = blogholicSQLiteHelper;
        this.blogCardClickListener = blogCardClickListener;
    }

    public LinkedList<BlogCardInfo> getBlogCardInfoList() {
        return blogCardInfoList;
    }

    @NonNull
    @Override
    public BlogCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BlogCardViewHolder((CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_card, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull BlogCardViewHolder holder, int position) {
        CardView blogCard = holder.blogCard;

        Cursor blogDataCursor = blogholicSQLiteHelper.getReadableDatabase().query(BlogholicSQLiteHelper.BlogTable.TABLE_NAME, BLOG_CARD_DATA_COLUMNS, "_id = ?", new String[]{Integer.toString(blogCardInfoList.get(position).getId())}, null, null, null);
        if(blogDataCursor.moveToFirst()){
            ((TextView)blogCard.findViewById(R.id.blog_card_title)).setText(blogDataCursor.getString(0));
            ((TextView)blogCard.findViewById(R.id.blog_card_date_label)).setText(blogDataCursor.getString(1));
            ((ImageView)blogCard.findViewById(R.id.blog_card_img)).setImageURI(Uri.parse(blogDataCursor.getString(2)));
        }
        blogDataCursor.close();

        blogCard.setOnClickListener(v -> {
            blogCardClickListener.onClickListener(holder.getAdapterPosition());
        });
        blogCard.setOnLongClickListener(v -> {
            BlogCardInfo blogCardInfo = blogCardInfoList.get(holder.getAdapterPosition());
            blogCardInfo.setSelected(!blogCardInfo.isSelected);
            if(blogCardInfo.isSelected){
                blogCard.findViewById(R.id.card_layout).setBackgroundColor(Color.argb(40,245, 193, 7));
            }else{
                blogCard.findViewById(R.id.card_layout).setBackgroundColor(0);
            }
            blogCard.setActivated(blogCardInfo.isSelected);
            blogCardClickListener.onLongClickListener(blogCardInfo.id, blogCardInfo.isSelected);
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return blogCardInfoList.size();
    }

    public static class BlogCardViewHolder extends RecyclerView.ViewHolder{

        private CardView blogCard;

        public BlogCardViewHolder(@NonNull CardView blogCardView) {
            super(blogCardView);
            this.blogCard = blogCardView;
        }
    }


    public interface BlogCardClickListener{
        void onClickListener(int position);
        void onLongClickListener(int id, boolean isSelected);
    }

    public static class BlogCardInfo{
        private int id;
        private boolean isSelected;

        public BlogCardInfo(int id){
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }
}
