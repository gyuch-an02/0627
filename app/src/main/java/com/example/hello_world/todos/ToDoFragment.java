package com.example.hello_world.todos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hello_world.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ToDoFragment extends Fragment {

    private CalendarView calendarView;
    private LinearLayout todoContainer;
    private FloatingActionButton addTodoButton;
    private TextView emptyTextView;
    private ArrayList<ToDoItem> toDoList;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        todoContainer = view.findViewById(R.id.todoContainer);
        addTodoButton = view.findViewById(R.id.addTodoButton);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        toDoList = new ArrayList<>();

        selectedDate = getCurrentDate();
        loadToDoList(selectedDate);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            saveToDoList(selectedDate);
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            loadToDoList(selectedDate);
        });

        addTodoButton.setOnClickListener(v -> showAddToDoDialog());

        return view;
    }

    private void loadToDoList(String date) {
        toDoList.clear();
        toDoList.addAll(SharedPreferencesHelper.loadToDoList(getContext(), date));
        sortToDoList();  // Ensure the list is sorted before updating the UI
        updateToDoContainer();
    }

    private void saveToDoList(String date) {
        SharedPreferencesHelper.saveToDoList(getContext(), date, toDoList);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(calendarView.getDate()));
    }

    private void showAddToDoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add To-Do");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                ToDoItem newItem = new ToDoItem(task, false);
                toDoList.add(newItem);
                sortToDoList();  // Sort after adding a new item
                saveToDoList(selectedDate);
                updateToDoContainer();
            } else {
                Toast.makeText(getContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showModifyDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Modify/Delete To-Do");

        final EditText input = new EditText(getContext());
        input.setText(toDoList.get(position).getTask());
        builder.setView(input);

        builder.setPositiveButton("Modify", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                toDoList.get(position).setTask(task);
                sortToDoList();  // Sort after modifying an item
                saveToDoList(selectedDate);
                updateToDoContainer();
            } else {
                Toast.makeText(getContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            toDoList.remove(position);
            sortToDoList();  // Sort after deleting an item
            saveToDoList(selectedDate);
            updateToDoContainer();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateToDoContainer() {
        todoContainer.removeAllViews();
        if (toDoList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            for (int i = 0; i < toDoList.size(); i++) {
                View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_todo, todoContainer, false);
                TextView toDoTextView = itemView.findViewById(R.id.todoTextView);
                CheckBox toDoCheckBox = itemView.findViewById(R.id.todoCheckBox);

                ToDoItem toDoItem = toDoList.get(i);
                toDoTextView.setText(toDoItem.getTask());
                toDoCheckBox.setChecked(toDoItem.isDone());

                final int position = i;
                toDoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toDoItem.setDone(isChecked);
                    sortToDoList();  // Sort after changing the done status
                    saveToDoList(selectedDate);
                    updateToDoContainer();
                });

                itemView.setOnLongClickListener(v -> {
                    showModifyDeleteDialog(position);
                    return true;
                });

                todoContainer.addView(itemView);
            }
        }
    }

    private void sortToDoList() {
        Collections.sort(toDoList, new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem o1, ToDoItem o2) {
                if (o1.isDone() == o2.isDone()) {
                    // If both items are either done or not done, sort by timestamp (newer first)
                    return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                } else {
                    // Otherwise, sort by done status (not done items first)
                    return Boolean.compare(o1.isDone(), o2.isDone());
                }
            }
        });
    }
}
