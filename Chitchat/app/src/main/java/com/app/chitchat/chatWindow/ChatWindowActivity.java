package com.app.chitchat.chatWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.chitchat.R;
import com.app.chitchat.chatList.MainActivity;
import com.app.chitchat.data.Chat;
import com.app.chitchat.data.Const;
import com.app.chitchat.data.Profile;
import com.app.chitchat.data.SimpleMessageBody;
import com.app.chitchat.data.firebaseData.Message;
import com.app.chitchat.databaseHandler.DatabaseHandler;
import com.app.chitchat.networkConnection.MessageConsumer;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;

public class ChatWindowActivity extends AppCompatActivity {

    public static final String USER_ID = "userId";
    public static final String CHAT_INDEX = "uIndex";
    public static Profile profile = null;
    private final int MSG_LIMIT = 15;
    private int chatIndex = -1;
    public static String myPhoneNumber;
    private LinkedList<com.app.chitchat.data.Message> msgList;
    private RecyclerView msgListRecyclerView;

    private DatabaseReference msgRef;
    private DatabaseReference onlineStatusRef;
    private DatabaseHandler dbHandler;
    private OnlineStatusListener onlineStatusListener;
    private BroadcastReceiver msgReceiver;

    private TextView onlineStatusField;
    private EditText msgField;

    private boolean hasBeenModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.chat_profile_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if(getIntent()!=null){
            chatIndex = getIntent().getIntExtra(CHAT_INDEX, -1);
            if(chatIndex==-1 || (profile = MainActivity.getChatLinkedList().get(chatIndex))==null){
                String chatUserPhnNum = getIntent().getStringExtra(USER_ID);
                DatabaseHandler dnHandler = new DatabaseHandler(this);
                profile = dnHandler.getUserBy(chatUserPhnNum, true);
                dnHandler.close();
                myPhoneNumber = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).getString(Const.USER_ID, null);
            }else{
                myPhoneNumber = MainActivity.phoneNumber;
            }
            if(profile==null){
                Toast.makeText(this, "Invalid chat :(", Toast.LENGTH_LONG).show();
            }else{
                onlineStatusField = toolbar.findViewById(R.id.online_status);
                ((TextView)toolbar.findViewById(R.id.chat_user_name)).setText(profile.getName());
                ImageView profileImg = toolbar.findViewById(R.id.chat_profile_img);
                Glide.with(this).load(Uri.parse(profile.getProfileImgPath())).placeholder(R.drawable.ic_user).into(profileImg);
                registerOnlineStatusListener();
                msgField = findViewById(R.id.msg_input);
                msgRef = FirebaseDatabase.getInstance().getReference(Const.MSG_REF);
                dbHandler = new DatabaseHandler(this);
                msgListRecyclerView = findViewById(R.id.msg_list_recycler_view);
                loadMessages();
                registerMsgReceiver();
            }
        }else{
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_window_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.clear_chat_item){
            clearChat();
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerMsgReceiver(){
        msgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent!=null){
                    long msgId = intent.getLongExtra(MessageConsumer.MSG_ID, -1);
                    if(msgId!=-1){
                        com.app.chitchat.data.Message newMsg = dbHandler.getMessageById(profile.get_id(), msgId);
                        if(newMsg!=null){
                            addMsg(newMsg);
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(MessageConsumer.ACTION_MSG_LISTENER);
        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, intentFilter);
    }

    private void registerOnlineStatusListener() {
        onlineStatusRef = FirebaseDatabase.getInstance()
                .getReference(Const.USERS_REF)
                .child(profile.get_id())
                .child(Const.ONLINE_STATUS_REF);
        onlineStatusField.setText("");
        onlineStatusListener = new OnlineStatusListener();
        onlineStatusRef.addValueEventListener(onlineStatusListener);
    }

    public void loadMessages(){
        msgList = dbHandler.getAllMessages(profile.get_id(), String.valueOf(MSG_LIMIT));
        if(msgList==null){
            msgList = new LinkedList<>();
        }else{
            if(chatIndex!=-1){
                Chat chat = MainActivity.getChatLinkedList().get(chatIndex);
                if(chat.getUnreadMsgCount()!=0){
                    hasBeenModified = true;
                    dbHandler.resetUnreadMsgCount(profile.get_id());
                    chat.setUnreadMsgCount(0);
                }
            }
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        msgListRecyclerView.setLayoutManager(linearLayoutManager);
        msgListRecyclerView.setAdapter(new MsgRecyclerViewAdapter(msgList));
        msgListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!recyclerView.canScrollVertically(-1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    int beforeLength = msgList.size();
                    if(msgList.size()>0){
                        String time = msgList.getLast().getTime();
                        dbHandler.getAllMessagesLessThan(profile.get_id(), time, String.valueOf(MSG_LIMIT), msgList);
                    }else{
                        dbHandler.getAllMessages(profile.get_id(), String.valueOf(MSG_LIMIT), msgList);
                    }
                    if(msgList.size()!=beforeLength){
                        msgListRecyclerView.getAdapter().notifyItemRangeInserted(beforeLength, msgList.size() - beforeLength);
                    }else{
                        msgListRecyclerView.removeOnScrollListener(this);
                    }
                }
            }
        });
    }

    public void sendMsg(View view) {
        String msg = msgField.getText().toString().trim();
        if(!msg.isEmpty()){
            msgField.setText("");
            long msgId = sendMsg( msgRef, dbHandler, profile.get_id(), myPhoneNumber, new Message(0, msg));
            if(msgId!=-1){
                com.app.chitchat.data.Message newMsgData = dbHandler.getMessageById(profile.get_id(), msgId);
                if(newMsgData!=null)
                    addMsg(newMsgData);
            }else{
                Toast.makeText(this, "DB Error: Unable to insert msg", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addMsg(com.app.chitchat.data.Message msg){
        hasBeenModified = true;
        msgList.addFirst(msg);
        System.out.println(msgList);
        msgListRecyclerView.getAdapter().notifyItemInserted(0);
        msgListRecyclerView.scrollToPosition(0);
    }

    public static long sendMsg(DatabaseReference msgRef, DatabaseHandler dbHandler, String to, String from, Message msg){
        msgRef.child(to).child(from).push().setValue(msg);
        return dbHandler.insertMessage(to, from, msg, false);
    }

    public static long sendMsg(Context content, String to, String from, Message msg){
        DatabaseReference msgRef = FirebaseDatabase.getInstance().getReference(Const.MSG_REF);
        DatabaseHandler dbHandler = new DatabaseHandler(content);
        long msgId = sendMsg(msgRef, dbHandler, to, from, msg);
        dbHandler.close();
        return msgId;
    }

    public void clearChat(){
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("All data will be lost!\nAre you sure?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHandler.clearAllMessages(profile.get_id());
                    int len = msgList.size();
                    msgList.clear();
                    msgListRecyclerView.getAdapter().notifyItemRangeRemoved(0, len);
                    hasBeenModified = true;
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }

    @Override
    public void onBackPressed() {
        if(chatIndex!=-1){
            if(hasBeenModified && msgList.size()>0){
                MainActivity.getChatLinkedList().get(chatIndex).setLastMsgId(msgList.getFirst().get_id());
                setResult(RESULT_OK);
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        profile = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        onlineStatusRef.removeEventListener(onlineStatusListener);
        dbHandler.close();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
        super.onDestroy();
    }

    public class OnlineStatusListener implements ValueEventListener{
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                Boolean isOnline = snapshot.getValue(Boolean.class);
                if(isOnline!=null){
                    if(isOnline){
                        String online ="Online";
                        onlineStatusField.setText(online);
                    }else{
                        onlineStatusRef.getParent().child(Const.LAST_SEEN_REF)
                                .get()
                                .addOnCompleteListener(task->{
                                    if(task.isSuccessful()){
                                        String lastSeen = task.getResult().getValue(String.class);
                                        onlineStatusField.setText(lastSeen);
                                    }
                                });
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }

    }
}