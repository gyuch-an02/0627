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

    public static void saveToDoList(Context context, String date, ArrayList<ToDoItem> toDoList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(toDoList);
        editor.putString(TODO_LIST_KEY + date, json);
        editor.apply();
    }

    public static ArrayList<ToDoItem> loadToDoList(Context context, String date) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(TODO_LIST_KEY + date, null);
        Type type = new TypeToken<ArrayList<ToDoItem>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }
}
