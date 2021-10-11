package com.app.chitchat.data;

public class Chat extends Profile {
    private int pos;
    private int unreadMsgCount;
    private int lastMsgId;

    public Chat(){super();}

    public Chat(Profile profile, int pos){
        this(profile, pos, false);
    }
    public Chat(Profile profile, int pos, boolean isGroup){
        this(profile.get_id(), profile.getName(), profile.getProfileImgPath(), profile.getDescription(), pos, isGroup);
    }

    public Chat(String _id, String name, String profileImgPath,String description, int pos, boolean isGroup) {
        this(_id, name, profileImgPath, description, pos, 0, -1, isGroup);
    }

    public Chat(String _id, String name, String profileImgPath, String description,int pos, int unreadMsgCount, int lastMsgId, boolean isGroup) {
        super(_id, name, profileImgPath, description, isGroup);
        this.pos = pos;
        this.unreadMsgCount = unreadMsgCount;
        this.lastMsgId = lastMsgId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getUnreadMsgCount() {
        return unreadMsgCount;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }

    public int getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(int lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "pos=" + pos +
                ", unreadMsgCount=" + unreadMsgCount +
                ", lastMsgId=" + lastMsgId +
                '}';
    }
}
