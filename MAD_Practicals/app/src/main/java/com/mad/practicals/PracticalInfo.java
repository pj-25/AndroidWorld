package com.mad.practicals;

public class PracticalInfo {
    private int _id;
    private String label;
    private String aim;
    private String launcherActivity;
    private int imgId;

    public PracticalInfo(int _id, String label, String aim, String launcherActivity, int imgId) {
        this._id = _id;
        this.label = label;
        this.aim = aim;
        this.launcherActivity = launcherActivity;
        this.imgId = imgId;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAim() {
        return aim;
    }

    public void setAim(String aim) {
        this.aim = aim;
    }

    public String getLauncherActivity() {
        return launcherActivity;
    }

    public void setLauncherActivity(String launcherActivity) {
        this.launcherActivity = launcherActivity;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    @Override
    public String toString() {
        return "PracticalInfo{" +
                "_id=" + _id +
                ", label='" + label + '\'' +
                ", aim='" + aim + '\'' +
                ", launcherActivity='" + launcherActivity + '\'' +
                ", imgId=" + imgId +
                '}';
    }
}
