package com.example.hello_world;

public class PhotoItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private String text;
    private String path;
    private int type;

    public PhotoItem(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public PhotoItem(String path) {
        this.path = path;
        this.type = TYPE_ITEM;
    }

    public String getText() {
        return text;
    }

    public String getPath() {
        return path;
    }

    public int getType() {
        return type;
    }
}