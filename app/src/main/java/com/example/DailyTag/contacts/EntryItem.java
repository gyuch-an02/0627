package com.example.DailyTag.contacts;

import android.media.Image;
import android.widget.ImageView;
import android.graphics.Bitmap;

public class EntryItem {
    public static final int TYPE_DIARY = 0;
    public static final int TYPE_TODO = 1;
    public static final int TYPE_IMAGE = 2;

    private final int type;
    private String text;
    private Bitmap bitmap;

    public EntryItem(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public EntryItem(int type, Bitmap bitmap) {
        this.type = type;
        this.bitmap = bitmap;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
