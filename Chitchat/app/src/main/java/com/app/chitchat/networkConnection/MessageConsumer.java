package com.app.chitchat.networkConnection;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.chitchat.chatWindow.ChatWindowActivity;
import com.app.chitchat.R;
import com.app.chitchat.chatList.MainActivity;
import com.app.chitchat.data.Chat;
import com.app.chitchat.data.Const;
import com.app.chitchat.data.Profile;
import com.app.chitchat.data.firebaseData.Message;
import com.app.chitchat.databaseHandler.DatabaseHandler;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessageConsumer implements ChildEventListener, ValueEventListener {

    private DatabaseHandler dbHandler = null;
    private final NotificationManager notificationManager;
    private DatabaseReference msgRef;
    private final Context context;
    private boolean isRunning;
    private final String NOTIFICATION_CHANNEL_ID = "chitchat_msg_channel";
    private final Object waitLock;

    public MessageConsumer(Context context){
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        waitLock = new Object();
    }

    public void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Chitchat message notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notifies incoming messages");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public boolean run(){
        String phoneNumber = context.getSharedPreferences(Const.USER_DATA_PREF, Context.MODE_PRIVATE).getString(Const.USER_ID, null);
        if(phoneNumber!=null){
            isRunning = true;
            new Thread(()->{
                registerMessageListener(phoneNumber);
                synchronized (waitLock){
                    try {
                        while (isRunning)
                            waitLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        return true;
        }
        return false;
    }

    public void registerMessageListener(String phoneNumber){
        dbHandler = new DatabaseHandler(context);
        msgRef = FirebaseDatabase.getInstance()
                .getReference(Const.MSG_REF)
                .child(phoneNumber);
        msgRef.addValueEventListener(this);
        FirebaseDatabase.getInstance()
                .getReference(Const.USERS_REF)
                .child(phoneNumber)
                .child(Const.ONLINE_STATUS_REF)
                .onDisconnect()
                .setValue(false);
        //notifyStatus("Message Listener registered", 0);
    }

    public void consumeMsg(String fromUserId, Message msg){
        //Log.d("MSG", fromUserId+": "+msg);
        //notifyStatus("Message received from "+fromUserId, 1);
        Profile profile = dbHandler.getUserBy(fromUserId, false);
        if(profile == null){
            FirebaseDatabase.getInstance().getReference(Const.USERS_REF)
                    .child(fromUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Profile newProfile = task.getResult().getValue(Profile.class);
                            if(newProfile !=null){
                                newProfile.set_id(fromUserId);
                                consumeMsg(newProfile, msg);
                                long id = dbHandler.insertChat(new Chat(newProfile, 0));
                                if(id==-1){
                                    Log.d("CHAT INSERT Error", "phnNum: "+ newProfile.get_id());
                                }else{
                                    consumeMsg(newProfile, msg);
                                }
                            }
                            else{
                                Log.d("User FETCH error", fromUserId);
                            }
                        }
                        else{
                            Log.d("User FETCH failed", fromUserId);
                        }
                    });
        }else{
            consumeMsg(profile, msg);
        }
    }

    public void consumeMsg(Profile profile, Message msg){
        long id = dbHandler.insertMessage(profile.get_id(), msg);
        if(id == -1){
            notifyStatus("Error: enable to insert msg from "+profile.get_id(), -1);
        }else{
            if(!MainActivity.isForeground && (ChatWindowActivity.profile==null || !profile.get_id().equals(ChatWindowActivity.profile.get_id())))
                notifyMsg(profile, msg, id);
        }
    }

    public void notifyMsg(Profile profile, Message msg, long msgId){
        Intent chatIntent = new Intent(context, ChatWindowActivity.class);
        //chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        chatIntent.putExtra(ChatWindowActivity.USER_ID, profile.get_id());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, chatIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_notification)
                .setContentTitle(profile.getName())
                .setContentText(msg.getContent())
                .setSubText(msg.getTime())
                .setContentIntent(pendingIntent)
                .setGroup(profile.get_id())
                .setAutoCancel(true);
        notificationManager.notify((int) msgId, builder.build());
    }

    public void notifyStatus(String msg, int id){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_notification)
                .setContentTitle("Chitchat Job Status")
                .setContentText(msg);
        notificationManager.notify(id, builder.build());
    }

    public void consumeMsgFrom( String from, DataSnapshot messages){
        for(DataSnapshot msgSnapshot:messages.getChildren()){
            try {
                Message msg = msgSnapshot.getValue(Message.class);
                consumeMsg(from, msg);
            }catch (DatabaseException e){
                System.out.println(msgSnapshot);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        String from = snapshot.getKey();
        if(from!=null){
            consumeMsgFrom(from, snapshot);
            msgRef.removeEventListener((ChildEventListener) this);
            msgRef.child(from).removeValue((error, ref) -> {
                msgRef.addChildEventListener(MessageConsumer.this);
            });
        }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for(DataSnapshot fromSnapshot:snapshot.getChildren()){
            String from = fromSnapshot.getKey();
            consumeMsgFrom(from, fromSnapshot);
        }
        msgRef.removeEventListener((ValueEventListener) this);
        msgRef.removeValue((error, ref) -> {
            msgRef.addChildEventListener(MessageConsumer.this);
        });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    public void close(){
        isRunning = false;
        synchronized (waitLock){
            waitLock.notifyAll();
        }
        if(dbHandler!=null){
            dbHandler.close();
        }
        if(msgRef!=null){
            msgRef.removeEventListener((ChildEventListener) this);
        }
    }

}
