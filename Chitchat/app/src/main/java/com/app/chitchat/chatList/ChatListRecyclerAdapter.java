package com.app.chitchat.chatList;

import android.graphics.Typeface;
import android.net.Uri;
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
    private ChatClickListener chatClickListener;

    public ChatListRecyclerAdapter(LinkedList<Chat> chatLinkedList, DatabaseHandler dbHandler, ChatClickListener chatClickListener){
        this.chatLinkedList = chatLinkedList;
        this.dbHandler = dbHandler;
        this.chatClickListener = chatClickListener;
    }

    @NonNull
    @Override
    public ChatCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatCardViewHolder((CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row_card_view, parent, false), chatClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatCardViewHolder holder, int position) {
        Chat chatData = chatLinkedList.get(position);
        holder.setItemClickListener(chatClickListener);
        ConstraintLayout view = holder.cardView.findViewById(R.id.chat_row_container);
        ImageView profileImage = view.findViewById(R.id.chat_dp);
        Glide.with(holder.itemView.getContext()).load(Uri.parse(chatData.getProfileImgPath())).placeholder(R.drawable.ic_user).into(profileImage);
        TextView name = view.findViewById(R.id.chat_name);
        name.setText(chatData.getName());
        Message lastMsgData = dbHandler.getMessageById(chatData.get_id(), chatData.getLastMsgId());
        TextView lastMsgTime = view.findViewById(R.id.chat_last_msg_time);
        TextView unreadMsgCount = view.findViewById(R.id.chat_unread_msg_count);
        if(chatData.getLastMsgId() != -1){
            String content = dbHandler.getSimpleMsgContent(chatData.get_id(), chatData.getLastMsgId());
            TextView lastMsg = view.findViewById(R.id.chat_last_msg);
            if(content!=null){
                if(!lastMsgData.getFrom().equals(chatData.get_id())){
                    content = "you: " + content;
                }
                lastMsg.setText(content);
                lastMsgTime.setText(lastMsgData.getTime());
                if(chatData.getUnreadMsgCount()!=0){
                    String ucount = String.valueOf(chatData.getUnreadMsgCount());
                    unreadMsgCount.setText(ucount);
                    name.setTypeface(name.getTypeface(), Typeface.BOLD);
                }
            }else{
                lastMsg.setText("");
                lastMsgTime.setText("");
                unreadMsgCount.setText("");
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

    public static class ChatCardViewHolder extends RecyclerView.ViewHolder{

        private CardView cardView;

        public ChatCardViewHolder(@NonNull CardView cardView, ChatClickListener chatClickListener) {
            super(cardView);
            this.cardView = cardView;
        }

        public void setItemClickListener(ChatClickListener chatClickListener){
            cardView.setOnClickListener(v -> {
                chatClickListener.onChatClick(getAdapterPosition());
            });
        }
    }

    public interface ChatClickListener{
        void onChatClick(int pos);
    }
}
