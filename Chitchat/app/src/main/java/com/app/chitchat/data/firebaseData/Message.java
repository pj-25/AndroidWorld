package com.app.chitchat.data.firebaseData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private int type;
    private String time;
    private String content;

    public Message(){}

    public Message(int type, String content) {
        this.content = content;
        this.type = type;
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
