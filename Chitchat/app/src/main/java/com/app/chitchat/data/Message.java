package com.app.chitchat.data;

public class Message {
    private int _id;
    private String from;
    private String time;
    private int type;

    public Message(int id){
        this(id, null, null, -1);
    }

    public Message(int _id, String from, String time, int type) {
        this._id = _id;
        this.time = time;
        this.from = from;
        this.type = type;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public enum MessageType{
        TEXT,
        LINK,
        IMAGE,
        STICKER,
        GIF,
        HYBRID
    }
}
