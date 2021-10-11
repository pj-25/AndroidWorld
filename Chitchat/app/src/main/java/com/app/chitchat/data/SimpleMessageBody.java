package com.app.chitchat.data;

public class SimpleMessageBody implements MessageBody{
    private int _id;
    private String content;

    public SimpleMessageBody(int _id, String content) {
        this._id = _id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}
