package com.example.DailyTag.todos;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "todo_prefs";
    private static final String TODO_LIST_KEY = "todo_list_";
    private static final String DIARY_KEY = "diary_";
    private static final String KEY_DIARY_TAGS = "diaryTags";
    private static final String KEY_TODO_TAGS = "todoTags";

    // Save the to-do list for a specific date
    public static void saveToDoList(Context context, String date, ArrayList<ToDoItem> toDoList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(toDoList);
        editor.putString(TODO_LIST_KEY + date, json);
        editor.apply();
    }

    // Load the to-do list for a specific date
    public static ArrayList<ToDoItem> loadToDoList(Context context, String date) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(TODO_LIST_KEY + date, null);
        Type type = new TypeToken<ArrayList<ToDoItem>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    // Save the diary content for a specific date
    public static void saveDiaryContent(Context context, String date, String diaryContent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DIARY_KEY + date, diaryContent);
        editor.apply();
    }

    // Load the diary content for a specific date
    public static String loadDiaryContent(Context context, String date) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(DIARY_KEY + date, "");
    }

    // 다이어리 태그 저장
    public static void saveDiaryTags(Context context, Set<String> tags) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_DIARY_TAGS, tags);
        editor.apply();
    }

    // 다이어리 태그 불러오기
    public static Set<String> loadDiaryTags(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_DIARY_TAGS, new HashSet<>());
    }

    // 투두 태그 저장
    public static void saveToDoTags(Context context, Set<String> tags) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_TODO_TAGS, tags);
        editor.apply();
    }

    // 투두 태그 불러오기
    public static Set<String> loadToDoTags(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_TODO_TAGS, new HashSet<>());
    }
}
