package com.app.blogholic;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import com.app.blogholic.databaseHandler.BlogholicSQLiteHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int SHOW_BLOG_REQ_CODE = 0;
    private static final int CREATE_BLOG_REQ_CODE = 1;

    private BlogholicSQLiteHelper blogholicSQLiteHelper;
    private RecyclerView blogListRecyclerView;
    private LinkedList<BlogListAdapter.BlogCardInfo> blogCardInfoList;
    private int selectedBlogPos;

    private Set<Integer> selectedBlogIds;
    private boolean isSelectionModeOn;
    private MenuItem deleteItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogholic_main);

        Toolbar toolbar = findViewById(R.id.show_blog_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> launchCreateBlogActivity());

        loadDummyImage();

        BlogListAdapter.BlogCardClickListener blogCardClickListener = new BlogListAdapter.BlogCardClickListener() {
            @Override
            public void onClickListener(int position) {
                if(!isSelectionModeOn){
                    showBlog(position);
                }else{
                    Toast.makeText(MainActivity.this, "Selection mode on!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClickListener(int position, boolean isSelected) {
                selectBlogForDeletion(position, isSelected);
            }
        };

        selectedBlogIds = new HashSet<>();
        blogholicSQLiteHelper = new BlogholicSQLiteHelper(this);
        blogCardInfoList = getBlogCardInfoList();
        if(blogCardInfoList.size() == 0){
            Toast.makeText(this, "No Blog found! Create your first blog now :)", Toast.LENGTH_LONG).show();
        }else{
            removeDummyImg();
        }
        blogListRecyclerView = findViewById(R.id.content_main_container);
        blogListRecyclerView.setAdapter(new BlogListAdapter(blogCardInfoList, blogholicSQLiteHelper, blogCardClickListener));
        blogListRecyclerView.setLayoutManager((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)?new LinearLayoutManager(this):new GridLayoutManager(this, 2));
    }

    void loadDummyImage(){
        int []gifIds = {R.raw.usagyuuun_note, R.raw.cat_writing, R.raw.foodies_apple, R.raw.notes_write, R.raw.animated_cute};
        Random random = new Random();
        int randomChoice = random.nextInt(gifIds.length);
        Glide.with(this).load(gifIds[randomChoice]).into((ImageView)findViewById(R.id.dummy_img));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        deleteItem = menu.findItem(R.id.main_action_delete);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_action_delete) {
            deleteSelectedBlog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case SHOW_BLOG_REQ_CODE:
                if (data != null) {
                    if(resultCode == ShowBlogActivity.DELETE_RESULT_CODE){
                        int deletedBlogId = data.getExtras().getInt(ShowBlogActivity.BLOG_ID);
                        deleteBlog(deletedBlogId);
                    }else if(resultCode == ShowBlogActivity.EDIT_RESULT_CODE){
                        int editedBlogId = data.getExtras().getInt(ShowBlogActivity.BLOG_ID);
                        editBlog(editedBlogId);
                    }
                }
                break;
            case CREATE_BLOG_REQ_CODE:
                if(data != null){
                    if(resultCode == RESULT_OK){
                        if(blogCardInfoList.size()==0){
                            removeDummyImg();
                        }
                        int newBlogId = (int)data.getExtras().getLong(BlogInputActivity.BLOG_ID);
                        blogCardInfoList.addLast(new BlogListAdapter.BlogCardInfo(newBlogId));
                        blogListRecyclerView.getAdapter().notifyItemInserted(blogCardInfoList.size() - 1);
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void removeDummyImg(){
        ImageView dummyImg = findViewById(R.id.dummy_img);
        if(dummyImg!=null){
            ViewGroup parent = (ViewGroup) dummyImg.getParent();
            if(parent!=null){
                parent.removeView(dummyImg);
            }
        }
    }

    @Override
    protected void onDestroy() {
        blogholicSQLiteHelper.close();
        super.onDestroy();
    }

    public LinkedList<BlogListAdapter.BlogCardInfo> getBlogCardInfoList(){
        LinkedList<BlogListAdapter.BlogCardInfo> blogIdList = new LinkedList<>();
        Cursor blogIdDataCursor = blogholicSQLiteHelper.getReadableDatabase().query(BlogholicSQLiteHelper.BlogTable.TABLE_NAME, new String[]{BlogholicSQLiteHelper.BlogTable.ID}, null, null, null, null, null);
        if(blogIdDataCursor.moveToFirst()){
            while(blogIdDataCursor.moveToNext()){
                blogIdList.add(new BlogListAdapter.BlogCardInfo(blogIdDataCursor.getInt(0)));
            }
        }
        blogIdDataCursor.close();
        return blogIdList;
    }

    public void launchCreateBlogActivity(){
        Intent createBlogIntent = new Intent(this, BlogInputActivity.class);
        startActivityForResult(createBlogIntent, CREATE_BLOG_REQ_CODE);
    }

    public void showBlog(int blogPos){
        selectedBlogPos = blogPos;
        CardView cardView = (CardView) blogListRecyclerView.getChildAt(blogPos);
        Intent showBlogIntent = new Intent(this, ShowBlogActivity.class);
        showBlogIntent.putExtra(ShowBlogActivity.BLOG_ID, blogCardInfoList.get(blogPos).getId());
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && cardView!=null){
            View sharedImage = cardView.findViewById(R.id.blog_card_img);
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(this, sharedImage, "shared_img");
            startActivityForResult(showBlogIntent, SHOW_BLOG_REQ_CODE, activityOptions.toBundle());
        }else {
            startActivityForResult(showBlogIntent, SHOW_BLOG_REQ_CODE);
        }
    }

    public void editBlog(int editedBlogId){
        blogListRecyclerView.getAdapter().notifyItemChanged(selectedBlogPos);
    }

    public void deleteBlog(int deletedBlogId){
        blogCardInfoList.remove(selectedBlogPos);
        blogListRecyclerView.getAdapter().notifyItemRemoved(selectedBlogPos);
    }

    public void deleteSelectedBlog(){
        if(selectedBlogIds.size()>0){
            BlogholicSQLiteHelper blogholicSQLiteHelper = new BlogholicSQLiteHelper(this);
            blogholicSQLiteHelper.deleteBlog(selectedBlogIds.toArray(new Integer[0]));
            selectedBlogIds.clear();
            isSelectionModeOn = false;
            deleteItem.setVisible(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                blogCardInfoList.removeIf(BlogListAdapter.BlogCardInfo::isSelected);
            }else{
                recreate();
            }
            blogListRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public void selectBlogForDeletion(int id, boolean isSelected){
        if(isSelected){
            if(!isSelectionModeOn && selectedBlogIds.size() == 0){
                isSelectionModeOn = true;
                deleteItem.setVisible(true);
            }
            selectedBlogIds.add(id);
        }else{
            selectedBlogIds.remove(id);
            if(isSelectionModeOn && selectedBlogIds.size() == 0){
                isSelectionModeOn = false;
                deleteItem.setVisible(false);
            }
        }
    }
}