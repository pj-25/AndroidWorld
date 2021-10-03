package com.app.chitchat.chatList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import com.app.chitchat.ChatActivity;
import com.app.chitchat.LauncherActivity;
import com.app.chitchat.R;
import com.app.chitchat.data.Chat;
import com.app.chitchat.data.Const;
import com.app.chitchat.databaseHandler.DatabaseHandler;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private static LinkedList<Chat> chatLinkedList;
    public static String PHONE_NUMBER = null;
    private static DatabaseHandler dbHandler;
    private DatabaseReference msgsRef;
    private RecyclerView chatListRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createChat());

        PHONE_NUMBER = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).getString(Const.USER_ID, null);

        dbHandler = new DatabaseHandler(this);
        chatLinkedList = new LinkedList<>();
        Cursor cursor = dbHandler.getReadableDatabase().query(DatabaseHandler.CHAT_TABLE, DatabaseHandler.CHAT_COLUMNS, null, null, null, null,  String.valueOf(DatabaseHandler.CHAT_COLUMNS[4]));
        chatListRecyclerView = findViewById(R.id.chat_list_recycler_view);
        chatListRecyclerView.setAdapter(new ChatListRecyclerAdapter(chatLinkedList, dbHandler, cursor));
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_profile:
                showProfile();
            case R.id.action_chat_search:
                searchChat();
                break;
            case R.id.action_delete:
                deleteChats();
                break;
            case R.id.action_settings:
                openSettings();
            case R.id.action_logout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProfile() {

    }

    public void searchChat(){

    }

    public void deleteChats(){

    }

    public void openSettings(){

    }

    public void createChat(){
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    public void logout(){
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);

        confirmDialog.setTitle("Confirm?");
        confirmDialog.setMessage("Warning: All data will be lost!");
        confirmDialog.setPositiveButton("Logout", (dialog, which) -> {
            dialog.dismiss();
            AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    clearLoginPref();
                    jumpToLauncher();
                    this.deleteDatabase(DatabaseHandler.DB_NAME);
                }
            });
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        confirmDialog.show();
    }

    public void jumpToLauncher(){
        Intent launcherIntent = new Intent(this, LauncherActivity.class);
        startActivity(launcherIntent);
        finish();
    }

    public void clearLoginPref(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deleteSharedPreferences(Const.USER_DATA_PREF);
        }else{
            getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).edit().remove(Const.USER_ID).apply();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        dbHandler.close();
        dbHandler = null;
        super.onDestroy();
    }


}