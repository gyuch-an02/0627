package com.example.DailyTag.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TagManager {
    private static TagManager instance;
    private final Map<String, Set<String>> tags;

    private TagManager() {
        tags = new HashMap<>();
    }

    public static synchronized TagManager getInstance() {
        if (instance == null) {
            instance = new TagManager();
        }
        return instance;
    }

    public void addTag(String key, String tag) {
        if (!tags.containsKey(key)) {
            tags.put(key, new HashSet<>());
        }
        Objects.requireNonNull(tags.get(key)).add(tag);
    }

    public void removeTag(String key, String tag) {
        if (tags.containsKey(key)) {
            Objects.requireNonNull(tags.get(key)).remove(tag);
        }
    }

    public Set<String> getTags(String key) {
        return tags.getOrDefault(key, new HashSet<>());
    }

    public Map<String, Set<String>> getAllTags() {
        return tags;
    }

    public void clearTags(String key) {
        tags.remove(key);
    }

    public void clearAllTags() {
        tags.clear();
    }
}
