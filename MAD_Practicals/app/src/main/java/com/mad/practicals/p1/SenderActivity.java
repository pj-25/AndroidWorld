package com.mad.practicals.p1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mad.practicals.R;

public class SenderActivity extends AppCompatActivity {

    private EditText msgInput;
    private Button sendBtn;
    private LinearLayout msgContainer;

    public static final int REPLY_MSG_REQ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        Glide.with(this).load(R.raw.send_msg).into((ImageView)findViewById(R.id.send_msg_img_view));

        msgInput = findViewById(R.id.reply_msg_input_view);
        sendBtn = findViewById(R.id.reply_btn);
        msgContainer = findViewById(R.id.msg_container);

        if(getIntent().getExtras()!=null){
            String msg = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            if(msg!=null){
                msgInput.setText(msg);
            }
        }
    }

    public void sendMsg(View view){
        String msg = msgInput.getText().toString().trim();
        if(!msg.isEmpty()){
            addMsg(msg, Gravity.END);
            Intent receiverIntent = new Intent(this, ReceiverActivity.class);   //Explicit intent
            receiverIntent.putExtra(ReceiverActivity.MSG, msg);
            startActivityForResult(receiverIntent, REPLY_MSG_REQ);
        }
    }

    public void addMsg(String msg, int msgPos){
        TextView msgBox = new TextView(this);
        msgBox.setText(msg);
        msgBox.setGravity(msgPos);
        msgBox.setWidth(0);
        msgContainer.addView(msgBox);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REPLY_MSG_REQ){
            if(resultCode == Activity.RESULT_OK){
                if(data!=null){
                    String replyMsg = data.getExtras().get(ReceiverActivity.MSG).toString();
                    addMsg(replyMsg, Gravity.START);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}