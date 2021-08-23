package com.app.blogholic;

public class Blog {
    private int id;
    private String title;
    private String content;
    private String entryDate;
    private String imgResPath;

    public Blog(int id, String title, String content, String entryDate, String imgResPath) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.entryDate = entryDate;
        this.imgResPath = imgResPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgResPath() {
        return imgResPath;
    }

    public void setImgResPath(String imgResPath) {
        this.imgResPath = imgResPath;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }
}
