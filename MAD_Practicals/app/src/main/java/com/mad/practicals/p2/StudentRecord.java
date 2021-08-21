package com.mad.practicals.p2;


import android.graphics.drawable.Drawable;

public class StudentRecord {
    private String name;
    private String address;
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

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
