package com.example.DailyTag.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.Set;

public class TagViewModel extends AndroidViewModel {
    private static final String PREFS_NAME = "tags_prefs";
    private static final String TAGS_KEY = "tags_key";
    private final MutableLiveData<Set<String>> tagSet;

    public TagViewModel(@NonNull Application application) {
        super(application);
        tagSet = new MutableLiveData<>(new HashSet<>());
        loadTags();
    }

    public LiveData<Set<String>> getTagSet() {
        return tagSet;
    }

    public void addTag(String tag) {
        Set<String> currentTags = new HashSet<>(tagSet.getValue());
        currentTags.add(tag);
        tagSet.setValue(currentTags);
        saveTags(currentTags);
    }

    public void removeTag(String tag) {
        Set<String> currentTags = new HashSet<>(tagSet.getValue());
        currentTags.remove(tag);
        tagSet.setValue(currentTags);
        saveTags(currentTags);
    }

    public void setTags(Set<String> tags) {
        tagSet.setValue(tags);
        saveTags(tags);
    }

    private void saveTags(Set<String> tags) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(TAGS_KEY, tags);
        editor.apply();
    }

    private void loadTags() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        tagSet.setValue(tags);
    }
}
