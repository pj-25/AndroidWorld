package com.app.blogholic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.blogholic.databaseHandler.BlogholicSQLiteHelper;

public class ShowBlogActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private int blogID;
    private TextView dateView;
    private TextView contentView;
    private String content;
    private ImageView blogImage;

    public static final String BLOG_ID = "blog_id";
    public static final int DELETE_RESULT_CODE = 0;
    public static final int EDIT_RESULT_CODE = 1;
    public static final int EDIT_REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_blog);

        Toolbar toolbar = findViewById(R.id.show_blog_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.edit_fab).setOnClickListener(v->editBlog());

        contentView = findViewById(R.id.blog_content);
        dateView = findViewById(R.id.blog_entry_date_view);
        blogImage = findViewById(R.id.blog_app_bar_image);

        Integer extras = (Integer) getIntent().getExtras().get(BLOG_ID);
        if(extras == null){
            contentView.setText("No Data Found!");
        }
        else{
            blogID = extras;
            showBlog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blog_view_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setQueryHint("Search Text in this blog...");
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete_blog:
                deleteBlog();
                break;
            case R.id.action_share_blog:
                shareBlog();
                break;
            case R.id.action_print_blog:
                printBlog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == EDIT_REQ_CODE && resultCode == RESULT_OK){
            setResult(EDIT_RESULT_CODE);
            showBlog();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void editBlog(){
        Intent editIntent = new Intent(this, BlogInputActivity.class);
        editIntent.putExtra(BlogInputActivity.BLOG_ID, blogID);
        startActivityForResult(editIntent, EDIT_RESULT_CODE);
    }

    public void deleteBlog(){
        BlogholicSQLiteHelper blogholicSQLiteHelper = new BlogholicSQLiteHelper(this);
        blogholicSQLiteHelper.deleteBlogById(blogID);
        blogholicSQLiteHelper.close();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(BLOG_ID, blogID);
        setResult(DELETE_RESULT_CODE, resultIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }else{
            finish();
        }
    }

    public void shareBlog(){
        Intent shareToEmailIntent = new Intent(Intent.ACTION_SENDTO);
        shareToEmailIntent.setData(Uri.parse("mailto:"));
        shareToEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Blogholic: "+ getSupportActionBar().getTitle());
        shareToEmailIntent.putExtra(Intent.EXTRA_TEXT, "["+ dateView.getText() + "]\n\n" + contentView.getText());

        if(shareToEmailIntent.resolveActivity(getPackageManager())!=null){
            startActivity(shareToEmailIntent);
        }else{
            Toast.makeText(this, "Unable to resolve email intent", Toast.LENGTH_SHORT).show();
        }
    }

    public void searchInBlog(String searchText){
        Log.d("SEARCHED_TEXT", searchText);

        String blogContent = content;
        if(blogContent.contains(searchText)){
            String highlightedText = "<font color='yellow'>" + searchText + "</font>";
            blogContent = blogContent.replace(searchText, highlightedText);
            blogContent = blogContent.replace("\n", "<br>");
            contentView.setText(Html.fromHtml(blogContent));
        }
    }

    public void showBlog(){
        BlogholicSQLiteHelper blogholicSQLiteHelper = new BlogholicSQLiteHelper(this);
        Blog blogData = blogholicSQLiteHelper.getBlogById(blogID);
        if(blogData!=null){
            showBlog(blogData);
        }else{
            Toast.makeText(this, "Unable to fetch the blog!", Toast.LENGTH_SHORT).show();
        }
        blogholicSQLiteHelper.close();
    }

    public void showBlog(Blog blogData){
        getSupportActionBar().setTitle(blogData.getTitle());
        contentView.setText(blogData.getContent());
        content = blogData.getContent();
        dateView.setText(blogData.getEntryDate());
        blogImage.setImageURI(Uri.parse(blogData.getImgResPath()));
    }

    public void printBlog(){
        Toast.makeText(this, "Print service is under implementation!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //searchInBlog(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchInBlog(newText);
        return true;
    }
}