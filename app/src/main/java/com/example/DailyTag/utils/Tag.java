package com.example.DailyTag.utils;

public class Tag {
    private long contactId;
    private String contactName;
    private String tagName;

    public Tag(long contactId, String contactName, String tagName) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.tagName = tagName;
    }

    public long getContactId() {
        return contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public String getTagName() {
        return tagName;
    }
}
