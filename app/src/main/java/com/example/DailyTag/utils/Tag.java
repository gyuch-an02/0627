package com.example.DailyTag.utils;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return contactId == tag.contactId && tagName.equals(tag.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactId, tagName);
    }
}
