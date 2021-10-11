package com.app.chitchat.data;

import java.util.HashMap;

public class HybridMsg implements MessageBody{
    private HashMap<Integer, Integer> msgBodyIds;

    public HybridMsg(HashMap<Integer, Integer> msgBodyIds) {
        this.msgBodyIds = msgBodyIds;
    }

    public HashMap<Integer, Integer> getMsgBodyIds() {
        return msgBodyIds;
    }

    public void setMsgBodyIds(HashMap<Integer, Integer> msgBodyIds) {
        this.msgBodyIds = msgBodyIds;
    }
}
