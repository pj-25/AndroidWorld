package com.mad.practicals.p2_3_5;


import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

public class StudentRecord {
    @SerializedName("name")
    private String name;
    @SerializedName("address")
    private String address;
    @SerializedName("img_path")
    private String imagePath;

    private Drawable image;

    public StudentRecord(String name, String address, Drawable image) {
        this.name = name;
        this.address = address;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
