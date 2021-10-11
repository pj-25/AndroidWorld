package com.app.chitchat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.app.chitchat.chatList.MainActivity;
import com.app.chitchat.data.Const;
import com.app.chitchat.profile.UserProfileInput;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;

public class LauncherActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MainActivity.phoneNumber !=null){
            finish();
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE);
        String phoneNum = sharedPreferences.getString(Const.USER_ID, null);
        if(phoneNum != null){
            jumpToMainActivity();
        }else{
            jumpToSignIn();
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if(result.getResultCode() == RESULT_OK){

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                String phoneNumber = user.getPhoneNumber().substring(3);
                logUser(phoneNumber);
                jumpToCreateProfile();
            }else{
                Toast.makeText(this, "Something went wrong :(", Toast.LENGTH_SHORT).show();
                jumpToSignIn();
            }
        }else{
            Toast.makeText(this, "Unable to sign in", Toast.LENGTH_SHORT).show();
            jumpToSignIn();
        }
    }

    private void logUser(String phnNum){
        SharedPreferences.Editor editor = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).edit();
        editor.putString(Const.USER_ID, phnNum);
        editor.apply();
    }

    private void jumpToCreateProfile(){
        Intent createProfileIntent = new Intent(this, UserProfileInput.class);
        startActivity(createProfileIntent);
        finish();
    }

    private void jumpToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void jumpToSignIn(){
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.PhoneBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_Chitchat)
                .setLogo(R.mipmap.ic_launcher)
                .build();
        signInLauncher.launch(signInIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

}