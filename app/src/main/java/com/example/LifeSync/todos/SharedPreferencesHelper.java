package com.example.LifeSync.todos;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "todo_prefs";
    private static final String TODO_LIST_KEY = "todo_list_";
    private static final String DIARY_KEY = "diary_";

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
}
