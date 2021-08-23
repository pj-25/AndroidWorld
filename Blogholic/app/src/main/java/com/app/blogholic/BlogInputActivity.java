package com.app.blogholic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.blogholic.databaseHandler.BlogholicSQLiteHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlogInputActivity extends AppCompatActivity {

    private final int TAKE_IMG = 0;
    private final int PICK_IMG = 1;
    public static final String BLOG_ID = "blog_id";

    private ImageView blogImage;
    private EditText blogTitle;
    private EditText blogContent;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_input);

        blogImage = findViewById(R.id.blog_img);
        blogTitle = findViewById(R.id.blog_title_input);
        blogContent = findViewById(R.id.blog_content_input);
        findViewById(R.id.blog_img).setOnClickListener(this::onAddImage);

        Button submitBtn = findViewById(R.id.blog_submit_btn);

        if(savedInstanceState!=null){
            this.imagePath = savedInstanceState.getString("img-path");
        }

        Bundle taskExtras = getIntent().getExtras();
        if(taskExtras == null){
            submitBtn.setOnClickListener(v->createBlog());
            getSupportActionBar().setTitle("Create Blog");
        }else{
            int blogId = taskExtras.getInt(BLOG_ID);
            if(blogId > 0){
                fetchBlog(blogId);
                submitBtn.setOnClickListener(v->editBlog(blogId));
                String btnText = "EDIT";
                submitBtn.setText(btnText);
                getSupportActionBar().setTitle("Edit Blog");
            }else{
                displayToast("Invalid blog ID");
                submitBtn.setOnClickListener(v->createBlog());
            }
        }
    }

    public void onAddImage(View view){
        final String[] addOptions = {"Take Photo", "Pick from Gallery", "Cancel"};

        AlertDialog.Builder addImageDialogBuilder = new AlertDialog.Builder(this);
        addImageDialogBuilder.setTitle("Add Image");
        addImageDialogBuilder.setItems(addOptions, (dialog, which) -> {
            switch (which){
                case 0:
                    takePhoto();
                    break;
                case 1:
                    pickPhotoFromGallery();
                    break;
                case 2:
                    dialog.dismiss();
            }
        });

        addImageDialogBuilder.show();
    }

    public File createImageFile() throws IOException {
        String currentTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imgFileName = currentTimeStamp +"_captured_img";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imgFileName, ".jpg", dir);
        this.imagePath = image.getAbsolutePath();
        return image;
    }

    public void takePhoto(){
        try{
            File imageFile = createImageFile();
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePhotoIntent, TAKE_IMG);
        }catch (IOException ioException){
            ioException.printStackTrace();
            Toast.makeText(this, "Unable to create file!",Toast.LENGTH_SHORT).show();
        }
    }

    public void pickPhotoFromGallery(){
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, PICK_IMG);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TAKE_IMG:
                if(resultCode == RESULT_OK){
                    if(imagePath!=null){
                        blogImage.setImageURI(Uri.parse(imagePath));
                        System.out.println(imagePath);
                    }
                }else{
                    imagePath = null;
                }
                break;
            case PICK_IMG:
                if(resultCode == RESULT_OK && data != null){
                    Uri selectedImageData = data.getData();
                    if(selectedImageData != null){
                        String filePathColumn = MediaStore.Images.Media.DATA;
                        Cursor imageDataCursor = getContentResolver().query(selectedImageData, new String[]{filePathColumn}, null, null, null);
                        if(imageDataCursor.moveToFirst()){
                            int columnIndex = imageDataCursor.getColumnIndex(filePathColumn);
                            String imagePath = imageDataCursor.getString(columnIndex);
                            imageDataCursor.close();

                            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                                blogImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                            }
                            else{
                                ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
                                    if(isGranted){
                                        blogImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                                    }else{
                                        Toast.makeText(BlogInputActivity.this, "To pick photo from gallery, proved the permission.", Toast.LENGTH_LONG).show();
                                    }
                                });
                                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                            this.imagePath = imagePath;
                        }
                    }
                }
                else{
                    imagePath = null;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public boolean checkInputFields(){
        if(imagePath == null || imagePath.isEmpty()){
            displayToast("Please add an image!");
            blogImage.requestFocus();
            return false;
        }

        String title = blogTitle.getText().toString();
        if(title.isEmpty()){
            blogTitle.requestFocus();
            displayToast("Please enter blog title :(");
            return false;
        }

        String content = blogContent.getText().toString();
        if(content.isEmpty()){
            displayToast("Please write your blog!");
            blogContent.requestFocus();
            return false;
        }
        return true;
    }

    public void editBlog(int blogId){
        if(checkInputFields()){
            BlogholicSQLiteHelper blogholicSQLiteHelper = new BlogholicSQLiteHelper(this);
            if(blogholicSQLiteHelper.updateBlog(blogId, blogTitle.getText().toString(), blogContent.getText().toString(), imagePath)==0){
                displayToast("Unable to edit blog :(");
            }else{
                setResult(RESULT_OK);
                finish();
            }
            blogholicSQLiteHelper.close();
        }
    }


    public void fetchBlog(int blogId){
        BlogholicSQLiteHelper blogholicSQLiteHelper = new BlogholicSQLiteHelper(this);
        Blog blog = blogholicSQLiteHelper.getBlogById(blogId);
        if(blog != null){
            blogTitle.setText(blog.getTitle());
            blogContent.setText(blog.getContent());
            blogImage.setImageURI(Uri.parse(blog.getImgResPath()));
            this.imagePath = blog.getImgResPath();
        }
    }


    public void createBlog(){
        if(checkInputFields()){
            BlogholicSQLiteHelper blogholicSQLiteHelper = new BlogholicSQLiteHelper(this);
            long newBlogId = blogholicSQLiteHelper.insertBlog(blogTitle.getText().toString(), blogContent.getText().toString(), imagePath);
            if( newBlogId != -1){
                displayToast("Blog created successfully :)");
                setResult(RESULT_OK, new Intent().putExtra(BLOG_ID, newBlogId));
                finish();
            }else{
                displayToast("Enable to create blog :(");
            }
            blogholicSQLiteHelper.close();
        }
    }

    public void displayToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if(imagePath!=null)
            outState.putString("img-path", imagePath);
        super.onSaveInstanceState(outState);
    }
}