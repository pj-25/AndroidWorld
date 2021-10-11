package com.app.chitchat.chatWindow;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chitchat.R;
import com.app.chitchat.data.Message;
import com.app.chitchat.data.SimpleMessageBody;

import java.util.LinkedList;

public class MsgRecyclerViewAdapter extends RecyclerView.Adapter<MsgRecyclerViewAdapter.msgBoxHolder> {

    public LinkedList<com.app.chitchat.data.Message> msgList;

    public MsgRecyclerViewAdapter(LinkedList<Message> msgList) {
        this.msgList = msgList;
    }

    @NonNull
    @Override
    public msgBoxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new msgBoxHolder((LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_box_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull msgBoxHolder holder, int position) {
        Message msg = msgList.get(position);
        TextView msgTime = holder.msgBox.findViewById(R.id.msg_time);
        msgTime.setText(msg.getTime());
        LinearLayout linearLayout = holder.msgBox.findViewById(R.id.msg_content_box);
        if(msg.getType() == Message.MessageType.HYBRID.ordinal()){
            //TODO handler hybrid msg display
        }else{
            TextView msgContentView = new TextView(holder.msgBox.getContext());
            msgContentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            msgContentView.setText(((SimpleMessageBody)msg.getBody()).getContent());
            msgContentView.setGravity(Gravity.START);
            
            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            if(msg.getFrom().equals(ChatWindowActivity.myPhoneNumber)){
                layoutParams.gravity = Gravity.END;
            }else{
                layoutParams.gravity = Gravity.START;
            }
            holder.msgBox.findViewById(R.id.msg_card_view).setLayoutParams(layoutParams);
            if(linearLayout.getChildCount()>1)
                linearLayout.removeViewAt(1);
            linearLayout.addView(msgContentView);
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public class msgBoxHolder extends RecyclerView.ViewHolder {

        private LinearLayout msgBox;

        public msgBoxHolder(@NonNull LinearLayout msgBox) {
            super(msgBox);
            this.msgBox = msgBox;
        }
    }
}
