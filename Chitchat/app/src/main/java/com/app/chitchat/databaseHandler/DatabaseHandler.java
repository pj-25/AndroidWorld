package com.app.chitchat.databaseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.app.chitchat.data.Chat;
import com.app.chitchat.data.HybridMsg;
import com.app.chitchat.data.Message;
import com.app.chitchat.data.Profile;
import com.app.chitchat.data.SimpleMessageBody;

import java.util.LinkedList;


public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String CHAT_TABLE = "chat";
    public static final String[] CHAT_COLUMNS = {"_id", "name", "profile_img_path","description", "pos", "unread_msgs", "last_msg_id", "is_group"};
    public static final String[] PROFILE_COLUMNS = {"_id", "name", "profile_img_path","description", "is_group"};
    public static final String[] MSG_COLUMNS = {"_id", "from_user", "time", "type"};
    public static final String[] SIMPLE_MSG_BODY_COLUMNS = {"_id", "msg_id", "content"};
    public static final String[] HYBRID_MSG_COLUMNS = {"msg_id", "msg_body_id"};
    public static final String[] GROUP_MEMBER_COLUMNS = {"grp_id", "user_id"};
    public static final String GROUP_MEMBER_TABLE = "grp_member";


    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "chitchatDB";

    public DatabaseHandler(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        manageDatabaseSchema(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        manageDatabaseSchema(db, oldVersion, newVersion);
    }

    public static void manageDatabaseSchema(SQLiteDatabase db, int oldVer, int newVer){
        if(oldVer == 0){
            createChatTable(db);
            createGroupMemberTable(db);
        }
    }

    public static void createChatTable(SQLiteDatabase db){
        String q = "CREATE TABLE "+CHAT_TABLE+" (" +
                CHAT_COLUMNS[0]+ " TEXT PRIMARY KEY," +
                CHAT_COLUMNS[1]+ " TEXT NOT NULL," +
                CHAT_COLUMNS[2]+ " TEXT NOT NULL," +
                CHAT_COLUMNS[3]+ " TEXT NOT NULL," +
                CHAT_COLUMNS[4]+ " INTEGER NOT NULL," +
                CHAT_COLUMNS[5]+ " INTEGER DEFAULT(0)," +
                CHAT_COLUMNS[6]+ " INTEGER DEFAULT(-1)," +
                CHAT_COLUMNS[7]+ " BOOLEAN" +
                ")";
        db.execSQL(q);
    }


    public long insertChat(Chat chat){
        ContentValues chatData = new ContentValues();
        chatData.put(CHAT_COLUMNS[0], chat.get_id());
        chatData.put(CHAT_COLUMNS[1], chat.getName());
        chatData.put(CHAT_COLUMNS[2], chat.getProfileImgPath());
        chatData.put(CHAT_COLUMNS[3], chat.getDescription());
        chatData.put(CHAT_COLUMNS[4], chat.getPos());
        chatData.put(CHAT_COLUMNS[5], chat.getUnreadMsgCount());
        chatData.put(CHAT_COLUMNS[6], chat.getLastMsgId());
        chatData.put(CHAT_COLUMNS[7], chat.isGroup());
        long id;
        if((id = getWritableDatabase().insert(CHAT_TABLE, null, chatData))!=-1){
            initNewUser(chat.get_id());
        }
        return id;
    }

    public LinkedList<Chat> getAllChats(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor chatCursor = db.query(CHAT_TABLE, CHAT_COLUMNS, null, null, null, null, null);
        LinkedList<Chat> chatList = null;
        if(chatCursor!=null && chatCursor.moveToFirst()){
            chatList = new LinkedList<>();
            do{
                chatList.add(readChatFrom(chatCursor));
            }while (chatCursor.moveToNext());
            chatCursor.close();
        }
        return chatList;
    }

    public static Chat readChatFrom(Cursor chatCursor){
        return new Chat(chatCursor.getString(0),
                chatCursor.getString(1),
                chatCursor.getString(2),
                chatCursor.getString(3),
                chatCursor.getInt(4),
                chatCursor.getInt(5),
                chatCursor.getInt(6),
                chatCursor.getInt(7)==1);
    }

    public Profile getUserBy(String userId, boolean includeDescription){
        String []columns;
        if(includeDescription){
            columns = PROFILE_COLUMNS;
        }else{
            columns = new String[]{"_id", "name", "profile_img_path", "is_group"};
        }
        Cursor userCursor = getReadableDatabase().query(CHAT_TABLE, columns, CHAT_COLUMNS[0]+"=?", new String[]{userId}, null, null, null);
        Profile profile = null;
        if(userCursor!=null && userCursor.moveToFirst()){
            profile = readUserFrom(userCursor);
            userCursor.close();
        }
        return profile;
    }

    public Profile getUserBy(String userId){
        return getUserBy(userId, true);
    }

    public boolean deleteUser(String userId){
        return getWritableDatabase().delete(CHAT_TABLE, "_id=?", new String[]{userId}) == 0;
    }

    public String getUserNameBy(String userId){
        Cursor cursor = getReadableDatabase().query(CHAT_TABLE, new String[]{CHAT_COLUMNS[1]}, "_id=?", new String[]{userId}, null, null, null);
        String userName= null;
        if(cursor!=null && cursor.moveToFirst()){
            userName = cursor.getString(0);
            cursor.close();
        }
        return userName;
    }

    public boolean isExistingUser(String userId){
        Cursor cursor = getReadableDatabase().query(CHAT_TABLE, new String[]{CHAT_COLUMNS[0]}, "_id=?", new String[]{userId}, null, null,null);
        if(cursor!=null && cursor.getCount()!=0){
            cursor.close();
            return true;
        }
        return false;
    }

    public long insertMessage(String fromUserId, com.app.chitchat.data.firebaseData.Message msg) {
        try{
            long msgId = insertMessage(fromUserId, msg.getTime(), msg.getType());
            long msgBodyId = -1;
            if(msgId!=-1){
                if(msg.getType() != Message.MessageType.HYBRID.ordinal()){
                    msgBodyId = insertSimpleMsg(fromUserId, msgId, msg.getContent());
                }
                //TODO insert HYBRID msg implementation

                if(msgBodyId != -1){
                    updateLastMsgId(fromUserId, msgBodyId);
                    return msgId;
                }
            }
            return -1;
        }catch (SQLException e){
            Log.d("MSG INSERTION", e.getMessage());
            return -1;
        }
    }

    public int updateChatPos(String chatId, int pos){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHAT_COLUMNS[4], pos);
        return getWritableDatabase().update(CHAT_TABLE, contentValues, "_id=?", new String[]{chatId});
    }

    public int updateLastMsgId(String chatId, long msgId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHAT_COLUMNS[6], msgId);
        return getWritableDatabase().update(CHAT_TABLE, contentValues, " _id = ?", new String[]{chatId});
    }

    public void initNewUser(String userId){
        createMessageTable(userId);
        createSimpleMsgBodyTable(userId);
        createHybridMsgTable(userId);
    }

    public Profile readUserFrom(Cursor userCursor){
        if(userCursor.getColumnCount() == 4){
            return new Profile(userCursor.getString(0), userCursor.getString(1), userCursor.getString(2), userCursor.getInt(3)==1);
        }
        return new Profile(userCursor.getString(0), userCursor.getString(1), userCursor.getString(2), userCursor.getString(3), userCursor.getInt(4)==1);
    }

    public void createMessageTable(String userID){
        String q = "CREATE TABLE "+ getMessageTableName(userID) +" (" +
                MSG_COLUMNS[0]+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MSG_COLUMNS[1]+ " TEXT NOT NULL," +
                MSG_COLUMNS[2]+" DATETIME NOT NULL," +
                MSG_COLUMNS[3]+ " INTEGER NOT NULL )";
        getWritableDatabase().execSQL(q);
    }

    public Message getMessageById(String userId, long msgId){
        Cursor msgCursor = getReadableDatabase().query(getMessageTableName(userId), MSG_COLUMNS, MSG_COLUMNS[0]+"= ?", new String[]{String.valueOf(msgId)}, null, null, null);
        Message msg = null;
        if(msgCursor!=null && msgCursor.moveToFirst()){
            msg= readMsgFrom(msgCursor);
            msgCursor.close();
        }
        return msg;
    }

    public LinkedList<Message> getAllMessages(String userId, String limit){
        return getAllMessages(userId, limit, null);
    }

    public LinkedList<Message> getAllMessages(String userId, String limit, LinkedList<Message> msgList){
        Cursor msgCursor = getReadableDatabase().query(getMessageTableName(userId), MSG_COLUMNS, MSG_COLUMNS[1]+"= ?", new String[]{userId}, null, null, MSG_COLUMNS[2]+" DESC", limit);
        return getAllMessagesFrom(msgCursor, msgList);
    }

    public LinkedList<Message> getAllMessagesLessThan(String userId, String time, String limit, LinkedList<Message> msgList){
        Cursor msgCursor = getReadableDatabase().query(getMessageTableName(userId), MSG_COLUMNS, MSG_COLUMNS[1]+"= ? and "+MSG_COLUMNS[2]+" < ?", new String[]{userId, time}, null, null, MSG_COLUMNS[2]+" DESC", limit);
        return getAllMessagesFrom(msgCursor, msgList);
    }

    public void clearAllMessages(String userId){
        SQLiteDatabase db = getWritableDatabase();
        String table = getMsgBodyTableName(userId);
        db.delete(table, null, null);
        db.delete("sqlite_sequence", "name=?", new String[]{table});
        table = getMessageTableName(userId);
        db.delete(table, null, null);
        db.delete("sqlite_sequence", "name=?", new String[]{table});
        //db.delete(getHybridMsgTableName(userId), null, null);
        db.close();
    }

    public LinkedList<Message> getAllMessagesFrom(Cursor msgCursor, LinkedList<Message> msgList){
        if(msgCursor != null && msgCursor.moveToFirst()) {
            if(msgList==null)
                msgList = new LinkedList<>();
            do{
                msgList.addLast(readMsgFrom(msgCursor));
            }while (msgCursor.moveToNext());
        }
        return msgList;
    }

    private Message readMsgFrom(Cursor msgCursor) {
        Message msg = new Message(msgCursor.getInt(0), msgCursor.getString(1), msgCursor.getString(2), msgCursor.getInt(3));
        if(msg.getType() == Message.MessageType.HYBRID.ordinal()){
            //TODO handle hybrid msg fetch
        }else{
            SimpleMessageBody simpleMessageBody = getSimpleMsgBy(msg.getFrom(), msg.get_id());
            msg.setBody(simpleMessageBody);
        }
        return msg;
    }

    public long insertMessage(String fromUserId, String time, int type){
        ContentValues msgContentValues = new ContentValues();
        msgContentValues.put(MSG_COLUMNS[1], fromUserId);
        msgContentValues.put(MSG_COLUMNS[2], time);
        msgContentValues.put(MSG_COLUMNS[3], type);
        return getWritableDatabase().insert(getMessageTableName(fromUserId), null, msgContentValues);
    }

    public static String getMessageTableName(String userId){
        return "msg_"+userId;
    }

    public void createSimpleMsgBodyTable(String userId){
        String q = "CREATE TABLE "+getMsgBodyTableName(userId)+" (" +
                SIMPLE_MSG_BODY_COLUMNS[0] +" INTEGER PRIMARY KEY AUTOINCREMENT," +
                SIMPLE_MSG_BODY_COLUMNS[1] +" TEXT NOT NULL," +
                SIMPLE_MSG_BODY_COLUMNS[2] +" TEXT NOT NULL," +
                "FOREIGN KEY("+ SIMPLE_MSG_BODY_COLUMNS[1]+") REFERENCES "+getMsgBodyTableName(userId)+"("+MSG_COLUMNS[0]+") )";
        getWritableDatabase().execSQL(q);
    }

    public static String getMsgBodyTableName(String userId){
        return "msg_body_"+userId;
    }

    public SimpleMessageBody getSimpleMsgBy(String userId, int msgID){
        Cursor cursor = getReadableDatabase().query(getMsgBodyTableName(userId), new String[]{"_id", "content"}, "_id=?", new String[]{String.valueOf(msgID)}, null, null, null);
        SimpleMessageBody simpleMessageBody = null;
        if(cursor!=null && cursor.moveToFirst()){
            simpleMessageBody = readSimpleMsgFrom(cursor);
            cursor.close();
        }
        return simpleMessageBody;
    }

    public long insertSimpleMsg(String fromUserId, long msgId, String content){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SIMPLE_MSG_BODY_COLUMNS[1], msgId);
        contentValues.put(SIMPLE_MSG_BODY_COLUMNS[2], content);
        return getWritableDatabase().insert(getMsgBodyTableName(fromUserId), null, contentValues);
    }

    public String getSimpleMsgContent(String userId, int msgId){
        Cursor cursor = getReadableDatabase().query(getMsgBodyTableName(userId), new String[]{SIMPLE_MSG_BODY_COLUMNS[2]}, "_id = ?", new String[]{String.valueOf(msgId)}, null, null, null);
        String content = null;
        if(cursor!=null && cursor.moveToFirst()){
            content = cursor.getString(0);
            cursor.close();
        }
        return content;
    }

    private SimpleMessageBody readSimpleMsgFrom(Cursor cursor) {
        return new SimpleMessageBody(cursor.getInt(0), cursor.getString(1));
    }

    public void createHybridMsgTable(String userID){
        String q = "CREATE TABLE "+getHybridMsgTableName(userID)+" (" +
                HYBRID_MSG_COLUMNS[0]+" INTEGER NOT NULL," +
                HYBRID_MSG_COLUMNS[1]+" TEXT NOT NULL," +
                "FOREIGN KEY ("+HYBRID_MSG_COLUMNS[0]+") REFERENCES "+getMessageTableName(userID)+"("+MSG_COLUMNS[0]+")," +
                "FOREIGN KEY ("+HYBRID_MSG_COLUMNS[1]+") REFERENCES "+getMsgBodyTableName(userID)+"("+ SIMPLE_MSG_BODY_COLUMNS[0]+") )";
        getWritableDatabase().execSQL(q);
    }

    public static String getHybridMsgTableName(String userId){
        return "hybrid_msg_"+userId;
    }

    public static void createGroupMemberTable(SQLiteDatabase db){
        String q = "CREATE TABLE "+ GROUP_MEMBER_TABLE +" (" +
                GROUP_MEMBER_COLUMNS[0] +" TEXT NOT NULL," +
                GROUP_MEMBER_COLUMNS[1] +" TEXT NOT NULL," +
                "FOREIGN KEY ("+GROUP_MEMBER_COLUMNS[0]+") REFERENCES "+CHAT_TABLE+"("+CHAT_COLUMNS[0]+")," +
                "FOREIGN KEY ("+GROUP_MEMBER_COLUMNS[1]+") REFERENCES "+CHAT_TABLE+"("+CHAT_COLUMNS[0]+") )";
        db.execSQL(q);
    }

}
