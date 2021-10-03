package com.app.chitchat.profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.chitchat.R;
import com.app.chitchat.chatList.MainActivity;
import com.app.chitchat.data.Const;
import com.app.chitchat.data.Profile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UserProfileInput extends AppCompatActivity {

    private ImageView profileImgInput;
    private EditText phnNumInput;
    private EditText nameInput;
    private EditText descriptionInput;

    private Uri addedImageUri = null;
    private Uri downloadURL = null;

    ActivityResultLauncher<Intent> imageResultLauncher;
    ActivityResultLauncher<String> requestPermissionLauncher;
    ActivityResultLauncher<Uri> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_input);

        profileImgInput = findViewById(R.id.profile_image_input);
        phnNumInput = findViewById(R.id.phn_num_input);
        nameInput = findViewById(R.id.name_input);
        descriptionInput = findViewById(R.id.description_input);

        String phnNum = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).getString(Const.USER_ID, null);
        if(phnNum!=null){
            phnNumInput.setText(phnNum);
        }else{
            finish();
        }

        registerLaunchers();
    }

    public void registerLaunchers(){
        imageResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getData()!=null) {
                addedImageUri = result.getData().getData();
                profileImgInput.setImageURI(addedImageUri);
            }
        });
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted -> {
            if(isGranted){
                imageResultLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            }
            else{
                displayToast("Please allow permission to pick photo from gallery!");
            }
        });
        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), isTaken -> {
            if(!isTaken){
                addedImageUri = null;
            }else{
                profileImgInput.setImageURI(addedImageUri);
            }
        });
    }


    public void onInputSubmit(View view){
        if(isValidInputs()) {
            createUserProfile();
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
                    pickFromGallery();
                    break;
                case 2:
                    dialog.dismiss();
            }
        });

        addImageDialogBuilder.show();
    }

    public File createImageFile() throws IOException {
        String imgFileName = phnNumInput.getText().toString()+"_profile_chitchat";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imgFileName, ".jpg", dir);
    }

    public void takePhoto(){
        try{
            File imageFile = createImageFile();
            addedImageUri = FileProvider.getUriForFile(this, "com.app.chitchat.fileprovider", imageFile);
            takePhotoLauncher.launch(addedImageUri);
        }catch (IOException ioException){
            ioException.printStackTrace();
            Toast.makeText(this, "Unable to create file!",Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromGallery() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            imageResultLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public void createUserProfile(){
        /*
        DatabaseHandler dbHandler = new DatabaseHandler(this);
        if(dbHandler.insertChat(new Chat(phnNumInput.getText().toString(), nameInput.getText().toString(), descriptionInput.getText().toString(), addedImageUri.toString(), -1, false))==1){
            uploadImage();
            return true;
        }
        dbHandler.close();
         */
        SharedPreferences.Editor editor = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).edit();
        editor.putString(Const.NAME, nameInput.getText().toString());
        editor.putString(Const.DESCRIPTION, descriptionInput.getText().toString());
        editor.putString(Const.PROFILE_IMG_PATH, addedImageUri.toString());
        editor.apply();
        uploadData();
    }

    public void uploadUserProfile(){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Const.USERS_REF);
        Profile userProfile = new Profile(phnNumInput.getText().toString(), nameInput.getText().toString(), downloadURL.toString(), descriptionInput.getText().toString());
        userProfile.set_id(null);
        userRef.child(phnNumInput.getText().toString()).setValue(userProfile);
    }

    public void uploadData(){
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("profile_img/"+phnNumInput.getText().toString());
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading profile image");
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        try {
            imageRef.putStream(getContentResolver().openInputStream(addedImageUri))
                    .continueWithTask(task -> {
                        if(!task.isSuccessful()) {
                            progressDialog.dismiss();
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            displayToast("Profile image uploaded successfully!");
                            progressDialog.dismiss();
                            downloadURL = task.getResult();
                            uploadUserProfile();
                            jumpToMainActivity();
                        }else{
                            displayToast("Unable to upload profile image :(");
                        }
                    });
            progressDialog.show();
        } catch (FileNotFoundException e) {
            displayToast("Unable to load image!");
        }
    }

    private void jumpToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    private boolean isValidInputs() {
        if(addedImageUri==null){
            profileImgInput.requestFocus();
            displayToast("Please upload image!");
            return false;
        }
        if(nameInput.getText().toString().isEmpty()){
            nameInput.requestFocus();
            displayToast("Please enter your name!");
            return false;
        }
        if(descriptionInput.getText().toString().isEmpty()){
            descriptionInput.requestFocus();
            displayToast("Please enter short description!");
            return false;
        }
        return true;
    }

    public void displayToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}