package com.app.chitchat.data.firebaseData;

public class Profile extends com.app.chitchat.data.Profile {
    private boolean isOnline;
    private String lastSeen;

    public Profile(){
        super();
    }

    public Profile(String _id, String name, String profileImgPath, String description, boolean isGroup, boolean isOnline) {
        this(_id, name, profileImgPath, description, isGroup, isOnline, "-");
    }

    public Profile(String _id, String name, String profileImgPath, String description, boolean isGroup, boolean isOnline, String lastSeen) {
        super(_id, name, profileImgPath, description, isGroup);
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}
