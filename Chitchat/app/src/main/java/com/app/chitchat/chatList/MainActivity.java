package com.app.chitchat.chatList;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.app.chitchat.chatWindow.ChatWindowActivity;
import com.app.chitchat.LauncherActivity;
import com.app.chitchat.R;
import com.app.chitchat.data.Chat;
import com.app.chitchat.data.Const;
import com.app.chitchat.data.firebaseData.Profile;
import com.app.chitchat.databaseHandler.DatabaseHandler;
import com.app.chitchat.networkConnection.MessageConsumerService;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ChatListRecyclerAdapter.ChatClickListener {

    private static LinkedList<Chat> chatLinkedList;
    public static String phoneNumber = null;
    private static DatabaseHandler dbHandler;
    private DatabaseReference onlineStatusRef;
    private RecyclerView chatListRecyclerView;

    private int openedChatIndex = -1;
    private ActivityResultLauncher<Intent> chatLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode()==RESULT_OK){
            moveChatToTop(openedChatIndex);
            openedChatIndex = -1;
        }
    });

    public static boolean isForeground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).getString(Const.USER_ID, null);
        registerMessageConsumerService();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createChat());

        dbHandler = new DatabaseHandler(this);
        loadChatListData();
        /*
        msgConsumer = new MessageConsumer(this);
        msgConsumer.registerMessageListener(phoneNumber);
         */
        onlineStatusRef = FirebaseDatabase.getInstance().getReference(Const.USERS_REF).child(phoneNumber).child(Const.ONLINE_STATUS_REF);
    }

    public void moveChatToTop(int index){
        if(index==0){
            chatListRecyclerView.getAdapter().notifyItemChanged(0);
        }else{
            Chat openedChat = chatLinkedList.remove(index);
            chatListRecyclerView.getAdapter().notifyItemRemoved(index);
            chatLinkedList.addFirst(openedChat);
            chatListRecyclerView.getAdapter().notifyItemInserted(0);
        }
    }

    private void loadChatListData() {
        chatLinkedList =  dbHandler.getAllChats();
        if(chatLinkedList==null){
            chatLinkedList = new LinkedList<>();
        }
        chatListRecyclerView = findViewById(R.id.chat_list_recycler_view);
        chatListRecyclerView.setAdapter(new ChatListRecyclerAdapter(chatLinkedList, dbHandler, this));
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        updateOnlineStatus(true);
        super.onStart();
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

    public void registerMessageConsumerService(){
        boolean isJobRunning = MessageConsumerService.isRunning;
        if(!isJobRunning && !MessageConsumerService.isJobServiceOn(this, MessageConsumerService.JOB_ID)){
            JobInfo.Builder jobBuilder = new JobInfo.Builder(MessageConsumerService.JOB_ID, new ComponentName(this, MessageConsumerService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobBuilder.build());
            Toast.makeText(this, "Message consumer scheduled successfully!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Message consumer already scheduled!", Toast.LENGTH_SHORT).show();
        }
    }

    public static LinkedList<Chat> getChatLinkedList() {
        return chatLinkedList;
    }

    public static void setChatLinkedList(LinkedList<Chat> chatLinkedList) {
        MainActivity.chatLinkedList = chatLinkedList;
    }

    public void showProfile() {

    }

    public void searchChat(){

    }

    public void deleteChats(){
        cancelJobService();
        Toast.makeText(this, "Message service cancelled!", Toast.LENGTH_SHORT).show();
    }

    public void openSettings(){

    }

    public void openChat(int chatPos){
        Intent chatIntent = new Intent(this, ChatWindowActivity.class);
        chatIntent.putExtra(ChatWindowActivity.CHAT_INDEX, chatPos);
        openedChatIndex = chatPos;
        chatLauncher.launch(chatIntent);
    }

    public void openChat(String chatUserId){
        Intent chatIntent = new Intent(this, ChatWindowActivity.class);
        chatIntent.putExtra(ChatWindowActivity.USER_ID, chatUserId);
        startActivity(chatIntent);
    }

    public void createChat(){
        final EditText phnNumInput = new EditText(this);
        phnNumInput.setGravity(Gravity.CENTER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Start (Chit)Chat")
                .setMessage("Enter friend's phone number:")
                .setIcon(R.drawable.chitchat_logo)
                .setView(phnNumInput);

        builder.setPositiveButton("Create", (dialog, which) -> {
                    String fPhnNum = phnNumInput.getText().toString();
                    if(!fPhnNum.isEmpty()){
                        FirebaseDatabase.getInstance()
                                .getReference(Const.USERS_REF)
                                .child(fPhnNum)
                                .get()
                                .addOnCompleteListener(task->{
                                    if(task.isSuccessful()){
                                        Profile friendsProfile = task.getResult().getValue(Profile.class);
                                        if(friendsProfile!=null){
                                            Chat newChat = new Chat(friendsProfile, 0);
                                            newChat.set_id(fPhnNum);
                                            dbHandler.insertChat(newChat);
                                            addChat(newChat);
                                            dialog.dismiss();
                                        }else{
                                            builder.setMessage("Invite "+fPhnNum+" to ChitChat");
                                            builder.setNeutralButton("Invite", (dialog1, which1) -> {
                                                inviteToChitchat(fPhnNum);
                                                dialog1.dismiss();
                                                dialog.dismiss();
                                            });
                                        }
                                    }
                                });
                    }else{
                        phnNumInput.requestFocus();
                    }
                })
                .setNegativeButton("Cancel",(dialog, which) -> {
                    dialog.dismiss();
                } ).show();
    }

    public void addChat(Chat chat){
        chatLinkedList.addFirst(chat);
        chatListRecyclerView.getAdapter().notifyItemInserted(0);
    }

    public void inviteToChitchat(String phnNum){
        AlertDialog.Builder inviteDialog = new AlertDialog.Builder(this);
        //TODO create a dialog popup to show invitation options
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
                    cancelJobService();
                }
            });
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        confirmDialog.show();
    }

    private void updatePositions(){
        int i=0;
        for(Chat chat:chatLinkedList){
            dbHandler.updateChatPos(chat.get_id(), i++);
        }
    }

    private void updateOnlineStatus(boolean isOnline){
        isForeground = isOnline;
        onlineStatusRef.setValue(isOnline);
        if(!isOnline){
            String time = new SimpleDateFormat("yyyy.MM.dd - h:mm a", Locale.getDefault()).format(new Date());
            onlineStatusRef.getParent().child(Const.LAST_SEEN_REF).setValue(time);
        }
    }

    private void cancelJobService() {
        ((JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE)).cancel(MessageConsumerService.JOB_ID);
        MessageConsumerService.isRunning = false;
    }

    public void jumpToLauncher(){
        Intent launcherIntent = new Intent(this, LauncherActivity.class);
        startActivity(launcherIntent);
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
        finishAffinity();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        isForeground = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        updatePositions();
        updateOnlineStatus(false);
        dbHandler.close();
        dbHandler = null;
        super.onDestroy();
    }

    @Override
    public void onChatClick(int pos) {
        openChat(pos);
    }
}