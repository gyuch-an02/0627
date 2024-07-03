package com.example.DailyTag.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.DailyTag.todos.ToDoItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagRepository {

    private static TagRepository instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private TagRepository(Application application) {
        sharedPreferences = application.getSharedPreferences("tags_prefs", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized TagRepository getInstance(Application application) {
        if (instance == null) {
            instance = new TagRepository(application);
        }
        return instance;
    }

    public Set<Tag> loadTags(String identifier) {
        String json = sharedPreferences.getString(identifier, "");
        if (json.equalsIgnoreCase("")) {
            return new HashSet<>();
        }
        Type type = new TypeToken<Set<Tag>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveTags(String identifier, Set<Tag> tags) {
        String json = gson.toJson(tags);
        sharedPreferences.edit().putString(identifier, json).apply();
    }

    public List<ToDoItem> loadToDoList(String date) {
        String json = sharedPreferences.getString(date + "_todo", "");
        List<ToDoItem> toDoItems = gson.fromJson(json, new TypeToken<List<ToDoItem>>() {
        }.getType());
        return toDoItems != null ? toDoItems : new ArrayList<>();
    }

    public void saveToDoList(String date, List<ToDoItem> toDoList) {
        String json = gson.toJson(toDoList);
        sharedPreferences.edit().putString(date + "_todo", json).apply();
    }

    public String loadDiaryContent(String date) {
        return sharedPreferences.getString(date + "_diary_content", "");
    }

    public void saveDiaryContent(String date, String content) {
        Log.d("TagRepository", "Saved Diary Content with identifier " + date + "_diary_content");
        sharedPreferences.edit().putString(date + "_diary_content", content).apply();
    }

    public Map<String, ?> getAllEntries() {
        return sharedPreferences.getAll();
    }
}
