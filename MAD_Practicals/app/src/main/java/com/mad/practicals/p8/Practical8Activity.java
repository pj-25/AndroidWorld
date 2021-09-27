package com.mad.practicals.p8;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.mad.practicals.R;

public class Practical8Activity extends AppCompatActivity {

    private VideoView videoView;
    private ImageView playBtn;
    private EditText smsPhnNumInput;
    private EditText smsMsgInput;
    private EditText toEmailAddressInput;
    private EditText emailMsgInput;
    private EditText emailSubjectInput;
    private TextView locationTxtView;

    private MediaController videoController;
    private MediaPlayer audioPlayer;
    private int audioPos = 0;
    private SmsManager smsManager;
    private LocationManager locationManager;
    private ActivityResultLauncher<String> requestSMSPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical8);

        videoView = findViewById(R.id.videoView);
        playBtn = findViewById(R.id.play_btn);
        smsPhnNumInput = findViewById(R.id.sms_phone_number_input);
        smsMsgInput = findViewById(R.id.sms_msg_input);
        toEmailAddressInput = findViewById(R.id.to_email_input);
        emailMsgInput = findViewById(R.id.message_email_input);
        emailSubjectInput = findViewById(R.id.email_subject_input);
        locationTxtView = findViewById(R.id.location_text_view);

        playBtn.setOnClickListener(v -> audioAction());

        initVideoPlayer();
        intiAudioPlayer();

        smsManager = SmsManager.getDefault();
        requestSMSPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                sendSMS(null);
            } else {
                Toast.makeText(this, "Please grant permission to send SMS", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void initVideoPlayer() {
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.what_if_trailer));

        videoController = new MediaController(this);
        videoController.setMediaPlayer(videoView);
        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setOnVideoSizeChangedListener((mediaPlayer1, i, i1) -> {
                videoView.setMediaController(videoController);
                videoController.setAnchorView(videoView);
            });
        });
        videoView.start();
    }

    public void sendSMS(View view) {
        String smsPhnNum = smsPhnNumInput.getText().toString();
        if (smsPhnNum.isEmpty()) {
            smsPhnNumInput.requestFocus();
            Toast.makeText(this, "Please enter phone number!", Toast.LENGTH_SHORT).show();
            return;
        }
        String smsMsg = smsMsgInput.getText().toString();
        if (smsMsg.isEmpty()) {
            smsMsgInput.requestFocus();
            Toast.makeText(this, "Please enter message!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestSMSPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        } else {
            sendSMS(smsPhnNum, smsMsg);
        }
    }

    public void sendSMS(String phnNum, String msg) {
        smsManager.sendTextMessage(phnNum, null, msg, null, null);
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void sendEmail(View view) {
        String emailId = toEmailAddressInput.getText().toString();
        if (emailId.isEmpty()) {
            toEmailAddressInput.requestFocus();
            Toast.makeText(this, "Please enter email address!", Toast.LENGTH_SHORT).show();
        }
        String emailSubject = emailSubjectInput.getText().toString();
        if (emailSubject.isEmpty()) {
            emailSubjectInput.requestFocus();
            return;
        }
        String msg = emailMsgInput.getText().toString();
        if (msg.isEmpty()) {
            emailMsgInput.requestFocus();
            Toast.makeText(this, "Please enter message!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailId});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);
        if(emailIntent.resolveActivity(getPackageManager())!=null){
            startActivity(emailIntent);
        }
        else{
            Toast.makeText(this, "No activity found to send mail :(", Toast.LENGTH_SHORT).show();
        }
    }

    public void setCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String> reqPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
                if(isGranted){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location -> {
                        String loc = "Latitude: "+location.getLatitude()+" , Longitude: "+location.getLongitude();
                        locationTxtView.setText(loc);
                    });
                }else{
                    Toast.makeText(this, "Please grant permission to get current location", Toast.LENGTH_SHORT).show();
                }
            });
            reqPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location -> {
                String loc = "Latitude: "+location.getLatitude()+" , Longitude: "+location.getLongitude();
                locationTxtView.setText(loc);
            });
        }
    }

    @Override
    protected void onStart() {
        setCurrentLocation();
        super.onStart();
    }

    public void intiAudioPlayer(){
        audioPlayer = MediaPlayer.create(this, R.raw.say_you_wont_let_go_song);
    }

    public void audioAction(){
        if(!audioPlayer.isPlaying()){
            playBtn.setImageResource(R.drawable.ic_pause);
            audioPlayer.seekTo(audioPos);
            audioPlayer.start();
        }else{
            playBtn.setImageResource(R.drawable.ic_play);
            audioPos = audioPlayer.getCurrentPosition();
            audioPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        audioPlayer.release();
        audioPlayer = null;
        super.onDestroy();
    }
}