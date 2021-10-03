package com.app.chitchat.chatList;

import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chitchat.R;
import com.app.chitchat.data.Chat;
import com.app.chitchat.data.Message;
import com.app.chitchat.databaseHandler.DatabaseHandler;
import com.bumptech.glide.Glide;

import java.util.LinkedList;

public class ChatListRecyclerAdapter extends RecyclerView.Adapter<ChatListRecyclerAdapter.ChatCardViewHolder> {

    private LinkedList<Chat> chatLinkedList;
    private DatabaseHandler dbHandler;
    private Cursor cursor;

    public ChatListRecyclerAdapter(LinkedList<Chat> chatLinkedList, DatabaseHandler dbHandler, Cursor cursor){
        this.chatLinkedList = chatLinkedList;
        this.dbHandler = dbHandler;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public ChatCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatCardViewHolder((CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatCardViewHolder holder, int position) {
        Chat chatData = DatabaseHandler.readChatFrom(cursor);
        ConstraintLayout view = holder.cardView.findViewById(R.id.chat_row_container);
        ImageView profileImage = view.findViewById(R.id.chat_dp);
        Glide.with(holder.itemView.getContext()).load(chatData.getProfileImgPath()).into(profileImage);
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

    @Override
    public int getItemCount() {
        return chatLinkedList.size();
    }

    public LinkedList<Chat> getChatLinkedList() {
        return chatLinkedList;
    }

    public void setChatLinkedList(LinkedList<Chat> chatLinkedList) {
        this.chatLinkedList = chatLinkedList;
    }

    public DatabaseHandler getDbHandler() {
        return dbHandler;
    }

    public void setDbHandler(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public static class ChatCardViewHolder extends RecyclerView.ViewHolder{

        private final CardView cardView;

        public ChatCardViewHolder(@NonNull CardView cardView) {
            super(cardView);
            this.cardView = cardView;
        }
    }
}
