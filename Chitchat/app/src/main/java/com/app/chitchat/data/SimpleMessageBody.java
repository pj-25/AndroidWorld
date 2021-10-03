package com.app.chitchat.data;

public class SimpleMessageBody {
    private int _id;
    private int msgId;
    private String content;

    public SimpleMessageBody(){}

    public SimpleMessageBody(int _id, int msgId, String content) {
        this._id = _id;
        this.msgId = msgId;
        this.content = content;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
