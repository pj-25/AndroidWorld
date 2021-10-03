package com.app.chitchat.data.firebaseData;

public class Message {
    private int type;
    private String time;
    private String content;

    public Message(){}

    public Message(int type, String time, String content) {
        this.content = content;
        this.type = type;
        this.time = time;
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
}
