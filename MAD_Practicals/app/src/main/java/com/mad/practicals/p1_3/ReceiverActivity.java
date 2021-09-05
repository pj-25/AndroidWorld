package com.mad.practicals.p1_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mad.practicals.R;

public class ReceiverActivity extends AppCompatActivity {

    public static final String MSG = "_MSG";
    private EditText replyMsgInput;
    private TextView receivedMsgView;
    private EditText urlInput;
    private EditText phnNumInput;
    private EditText addressInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        replyMsgInput = findViewById(R.id.reply_msg_input_view);
        receivedMsgView = findViewById(R.id.received_msg);
        urlInput = findViewById(R.id.url_input);
        phnNumInput = findViewById(R.id.phn_num_input);
        addressInput = findViewById(R.id.address_input);

        if(getIntent().getExtras()!=null){
            String receivedMsg = getIntent().getExtras().getString(MSG);
            receivedMsgView.setText(receivedMsg);
        }
    }

    public void replyMsg(View view){
        String reply = replyMsgInput.getText().toString().trim();
        if(!reply.isEmpty()){
            Intent replyIntent = new Intent();
            replyIntent.putExtra(MSG, reply);
            setResult(RESULT_OK, replyIntent);
            finish();
        }else{
            replyMsgInput.requestFocus();
            replyMsgInput.setError("Please enter reply message!");
        }
    }

    public void viewInMap(View view){
        String location = addressInput.getText().toString().trim();
        if(!location.isEmpty()){
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+location));
            startActivity(viewIntent);
        }else{
            addressInput.requestFocus();
            addressInput.setError("Please enter address!");
        }
    }

    public void shareMsg(View view){
        String msg = receivedMsgView.getText().toString().trim();
        new ShareCompat.IntentBuilder(this).setType("text/plain").setText(msg).setChooserTitle("Share the text data...").startChooser();
    }

    public void jumpToURL(View view){
        String url = urlInput.getText().toString().trim();
        if(!url.isEmpty()){
            Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(urlIntent);
        }else{
            urlInput.requestFocus();
            urlInput.setError("Please enter url!");
        }
    }

    public void call(View view){
        String phnNum = phnNumInput.getText().toString().trim();
        if(!phnNum.isEmpty()){
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phnNum));
            startActivity(callIntent);
        }else{
            phnNumInput.requestFocus();
            phnNumInput.setError("Please enter phone number to call!");
        }
    }
}