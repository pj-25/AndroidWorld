package com.app.chitchat.data;

public class Profile {
    private String _id;
    private String name;
    private String profileImgPath;
    private String description;
    private boolean isGroup;

    public Profile(){}

    public Profile(String _id, String name, String profileImgPath, boolean isGroup){
        this(_id, name, profileImgPath, null, isGroup);
    }

    public Profile(String _id, String name, String profileImgPath, String description){
        this(_id, name, profileImgPath, description, false);
    }

    public Profile(String _id, String name, String profileImgPath, String description, boolean isGroup) {
        this._id = _id;
        this.name = name;
        this.description = description;
        this.profileImgPath = profileImgPath;
        this.isGroup = isGroup;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImgPath() {
        return profileImgPath;
    }

    public void setProfileImgPath(String profileImgPath) {
        this.profileImgPath = profileImgPath;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", profileImgPath='" + profileImgPath + '\'' +
                ", description='" + description + '\'' +
                ", isGroup=" + isGroup +
                '}';
    }
}
