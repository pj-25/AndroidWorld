package com.app.chitchat.networkConnection;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.chitchat.ChatActivity;
import com.app.chitchat.R;
import com.app.chitchat.data.Chat;
import com.app.chitchat.data.Const;
import com.app.chitchat.data.Profile;
import com.app.chitchat.data.firebaseData.Message;
import com.app.chitchat.databaseHandler.DatabaseHandler;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessageConsumerService extends JobService implements ChildEventListener {

    private DatabaseHandler dbHandler = null;
    private NotificationManager notificationManager;
    private DatabaseReference msgRef;

    private final String NOTIFICATION_CHANNEL_ID = "chitchat_msg_channel";
    private boolean hasPendingMsg = true;

    @Override
    public boolean onStartJob(JobParameters params) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        if(!registerMessageListener()){
            jobFinished(params, false);
        }
        return true;
    }

    public void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Chitchat message notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notifies incoming messages");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


    public boolean registerMessageListener(){
        dbHandler = new DatabaseHandler(getApplicationContext());
        String phoneNumber = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE).getString(Const.USER_ID, null);
        if(phoneNumber != null){
            msgRef = FirebaseDatabase.getInstance().getReference(phoneNumber).child(Const.MSG_REF);
            msgRef.addChildEventListener(this);
            return true;
        }
        return false;
    }

    public void consumeMsg(String fromUserId, Message msg){
        Profile profile = dbHandler.getUserBy(fromUserId, false);
        if(profile ==null){
            FirebaseDatabase.getInstance().getReference(Const.USERS_REF)
                    .child(fromUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Profile newProfile = task.getResult().getValue(Profile.class);
                            if(newProfile !=null){
                                newProfile.set_id(fromUserId);
                                consumeMsg(newProfile, msg);
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
        long id = dbHandler.insertChat(new Chat(profile, -1));
        if(id!=-1){
            Log.d("CHAT INSERT Error", "phnNum: "+ profile.get_id());
        }else{
            SharedPreferences sharedPreferences = getSharedPreferences(Const.USER_DATA_PREF, MODE_PRIVATE);
            boolean isOnline = sharedPreferences.getBoolean(Const.IS_ONLINE, false);
            String chatUser =  sharedPreferences.getString(Const.CHAT_USER, null);
            if(!isOnline || chatUser==null || !chatUser.equals(profile.getName()))
                notifyMsg(profile, msg, id);
        }
    }

    public void notifyMsg(Profile profile, Message msg, long msgId){
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        chatIntent.putExtra(ChatActivity.USER_ID, profile.get_id());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, chatIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.chitchat_logo)
                .setContentTitle("Chitchat")
                .setContentText(profile.getName())
                .setSubText(msg.getContent())
                .setContentIntent(pendingIntent)
                .setGroup(profile.get_id())
                .setAutoCancel(true);
        notificationManager.notify((int) msgId, builder.build());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if(dbHandler!=null){
            dbHandler.close();
        }
        if(msgRef!=null){
            msgRef.removeEventListener(this);
        }
        return true;
    }

    public static boolean isJobServiceOn( Context context, int jobID ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
        boolean hasBeenScheduled = false ;
        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() ==  jobID) {
                hasBeenScheduled = true ;
                break ;
            }
        }
        return hasBeenScheduled ;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        for(DataSnapshot messageSnapshot:snapshot.getChildren()){
            Message msg = messageSnapshot.getValue(Message.class);
            String from = messageSnapshot.getKey();
            consumeMsg(from, msg);
        }
        if(hasPendingMsg){
            hasPendingMsg = false;
            if(snapshot.exists()){
                msgRef.removeEventListener(this);
                msgRef.removeValue((error, ref) -> {
                    if(error!=null){
                        msgRef.addChildEventListener(this);
                    }
                });
            }
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
    public void onCancelled(@NonNull DatabaseError error) {

    }

}
