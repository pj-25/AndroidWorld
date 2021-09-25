package com.mad.practicals.p2_3_5_6;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad.practicals.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentRecordInputActivity extends AppCompatActivity {

    private final DatabaseReference studentDB = FirebaseDatabase.getInstance().getReference("student");
    private ImageView studentImg;
    private EditText nameInput;
    private EditText addressInput;
    private Button inputActionBtn;
    private Uri uploadedImageUri;
    private Uri addedImageUri = null;
    private String studentKey = null;

    ActivityResultLauncher<Intent> imageResultLauncher;
    ActivityResultLauncher<String> requestPermissionLauncher;
    ActivityResultLauncher<Uri> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_record_input);

        studentImg = findViewById(R.id.add_img_view);
        nameInput = findViewById(R.id.student_name_input);
        addressInput = findViewById(R.id.student_address_input);
        inputActionBtn = findViewById(R.id.input_action_btn);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            setTitle("Edit Student Record");
            inputActionBtn.setText("EDIT");
            nameInput.setText(intent.getExtras().getString("STUDENT_NAME"));
            addressInput.setText(intent.getExtras().getString("STUDENT_ADDRESS"));
            studentKey = intent.getExtras().getString("KEY");
            addedImageUri = Uri.parse(intent.getExtras().getString("IMAGE_PATH"));
            Glide.with(this).load(addedImageUri).into(studentImg);
        }
        studentImg.setOnClickListener(v -> {
            addImage();
        });

        registerLaunchers();
    }

    public void registerLaunchers(){
        imageResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getData()!=null) {
                addedImageUri = result.getData().getData();
                studentImg.setImageURI(addedImageUri);
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
            }
        });
    }

    public boolean checkInputs(){
        if(addedImageUri ==null){
            displayToast("Please add student image!");
            studentImg.requestFocus();
            return false;
        }
        String name = nameInput.getText().toString();
        if(name.isEmpty()){
            displayToast("Please enter student's name!");
            nameInput.requestFocus();
            return false;
        }
        String address = addressInput.getText().toString();
        if(address.isEmpty()){
            displayToast("Please enter student's address!");
            addressInput.requestFocus();
            return false;
        }
        return true;
    }

    public void onInputSubmitAction(View view){
        if(checkInputs()){
            try {
                if(studentKey!=null && getIntent().getExtras().getString("IMAGE_PATH").equals(addedImageUri.toString())){
                    editStudentRecord(new StudentRecord(nameInput.getText().toString(), addressInput.getText().toString(), addedImageUri.toString()));
                }else{
                    uploadImage(nameInput.getText().toString(), addedImageUri);
                }
            } catch (FileNotFoundException e) {
                displayToast("Error in loading image :(");
            }
        }
    }

    public void addImage(){
        String[] addImageOptions = {"Take Photo", "Pick from Gallery", "Cancel"};
        AlertDialog.Builder addImageOptionChooser = new AlertDialog.Builder(this);
        addImageOptionChooser.setItems(addImageOptions, (dialog, which) -> {
            dialog.dismiss();
            if(which == 0){
                takePhoto();
            }else{
                pickFromGallery();
            }
        });
        addImageOptionChooser.show();
    }

    private void pickFromGallery() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            imageResultLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public Uri createTempImageUri() throws IOException {
        String currentTimestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = currentTimestamp+"_captured_img";
        File image = File.createTempFile(imageFileName, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        return FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", image.getAbsoluteFile());
    }

    public void takePhoto(){
        try {
            addedImageUri = createTempImageUri();
            takePhotoLauncher.launch(addedImageUri);
        }catch (IOException e){
            displayToast("Unable to create image file");
        }
    }

    public void uploadImage(String imgName, Uri studentImageUri) throws FileNotFoundException {
        ProgressDialog uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setTitle("Uploading student data");
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        uploadProgressDialog.setMessage("Please wait!");
        StorageReference imageRef =  FirebaseStorage.getInstance().getReference("student_images/"+imgName);
        UploadTask imageUploadTask = imageRef.putStream(getContentResolver().openInputStream(studentImageUri));
        uploadProgressDialog.show();
        /*
        imageUploadTask.addOnProgressListener(snapshot -> {
            int currentProgress = (int)((snapshot.getBytesTransferred()*100.0)/snapshot.getTotalByteCount());
            uploadProgressDialog.setProgress(currentProgress);
        });
        */
        imageUploadTask.continueWithTask(task -> {
            if(!task.isSuccessful()) {
                uploadProgressDialog.dismiss();
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                uploadedImageUri = task.getResult();
                uploadProgressDialog.dismiss();
                createStudentRecord(new StudentRecord(nameInput.getText().toString(), addressInput.getText().toString(), uploadedImageUri.toString()));
            }
        });
    }

    public void editStudentRecord(StudentRecord studentRecord){
        studentDB.child(studentKey).setValue(studentRecord);
        finish();
    }

    public void createStudentRecord(StudentRecord studentRecord){
        studentDB.push().setValue(studentRecord);
        finish();
    }

    public void displayToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}