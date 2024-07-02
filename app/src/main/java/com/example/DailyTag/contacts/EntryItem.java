package com.example.DailyTag.contacts;

import android.widget.ImageView;

public class EntryItem {
    public static final int TYPE_DIARY = 0;
    public static final int TYPE_TODO = 1;
    public static final int TYPE_IMAGE = 2;

    private final int type;
    private String text;
    private ImageView imageView;

    public EntryItem(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public EntryItem(int type, ImageView imageView) {
        this.type = type;
        this.imageView = imageView;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
