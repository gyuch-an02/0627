package com.example.DailyTag.contacts;

import android.graphics.Bitmap;

public class EntryItem {
    public static final int TYPE_DIARY = 0;
    public static final int TYPE_TODO = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_HEADER = 3;
    public static final int TYPE_DATE = 4;
    public static final int TYPE_PADDING = 5;

    private final int type;
    private String date;
    private String content;
    private Bitmap bitmap;

    // Constructor for diary, to-do, and image items
    public EntryItem(int type, String content) {
        this.type = type;
        this.content = content;
    }

    public EntryItem(int type, Bitmap bitmap) {
        this.type = type;
        this.bitmap = bitmap;
    }

    // Constructor for header, date, and padding items
    public EntryItem(int type, String content, boolean isHeaderOrDate) {
        this.type = type;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
