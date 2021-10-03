package com.app.chitchat.chatList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.chitchat.R;
import com.app.chitchat.data.Chat;
import com.app.chitchat.data.Message;
import com.app.chitchat.databaseHandler.DatabaseHandler;
import com.bumptech.glide.Glide;

public class ChatListCursorAdapter extends CursorAdapter {

    private final DatabaseHandler dbHandler;

    public ChatListCursorAdapter(Context context, Cursor c, int flags, DatabaseHandler dbHandler) {
        super(context, c, flags);
        this.dbHandler = dbHandler;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.chat_row_card_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Chat chatData = DatabaseHandler.readChatFrom(cursor);
        ImageView profileImage = view.findViewById(R.id.chat_dp);
        Glide.with(context).load(chatData.getProfileImgPath()).into(profileImage);
        TextView name = view.findViewById(R.id.chat_name);
        name.setText(chatData.getName());
        Message lastMsgData = dbHandler.getMessageById(chatData.get_id(), chatData.getLastMsgId());
        if(chatData.getLastMsgId() != -1){
            String content = dbHandler.getSimpleMsgContent(chatData.get_id(), chatData.getLastMsgId());
            TextView lastMsg = view.findViewById(R.id.chat_last_msg);
            if(content!=null){
                content = lastMsgData.getFrom()+": "+content;
                lastMsg.setText(content);
                TextView lastMsgTime = view.findViewById(R.id.chat_last_msg_time);
                lastMsgTime.setText(lastMsgData.getTime());
                if(chatData.getUnreadMsgCount()!=0){
                    TextView unreadMsgCount = view.findViewById(R.id.chat_unread_msg_count);
                    unreadMsgCount.setText(chatData.getUnreadMsgCount());
                    name.setTypeface(name.getTypeface(), Typeface.BOLD);
                    lastMsg.setTypeface(lastMsg.getTypeface(), Typeface.BOLD);
                }
            }
        }
    }
}