package com.example.LifeSync.todos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.LifeSync.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ToDoFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private CalendarView calendarView;
    private LinearLayout todoContainer;
    private FloatingActionButton addTodoButton;
    private TextView emptyTextView;
    private TextView textTodo;
    private TextView textDiary;
    private AutoCompleteTextView diaryEditText;
    private ArrayList<ToDoItem> toDoList;
    private String selectedDate;
    private Boolean isTodoEmpty;
    private ArrayList<String> contacts;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        todoContainer = view.findViewById(R.id.todoContainer);
        addTodoButton = view.findViewById(R.id.addTodoButton);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        textTodo = view.findViewById(R.id.text_todo);
        textDiary = view.findViewById(R.id.text_diary);
        diaryEditText = view.findViewById(R.id.diaryEditText);
        isTodoEmpty = Boolean.TRUE;

        toDoList = new ArrayList<>();
        contacts = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, contacts);
        diaryEditText.setAdapter(adapter);

        selectedDate = getCurrentDate();
        loadToDoList(selectedDate);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            saveToDoList(selectedDate);
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            loadToDoList(selectedDate);
        });

        addTodoButton.setOnClickListener(v -> showAddToDoDialog());

        setUpToggle();
        requestContactsPermission();

        diaryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && start < s.length() && s.charAt(start) == '@') {
                    diaryEditText.showDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        setUpDiaryTextWatcher();

        return view;
    }

    private void setUpToggle() {
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        textDiary.setOnClickListener(v -> activateDiary());
        textTodo.setOnClickListener(v -> activateTodo());
    }

    private void activateTodo() {
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        diaryEditText.setVisibility(View.GONE);
        if (isTodoEmpty) {
            emptyTextView.setVisibility(View.VISIBLE);
        }
        todoContainer.setVisibility(View.VISIBLE);
        addTodoButton.setVisibility(View.VISIBLE);

        textDiary.setOnClickListener(v -> activateDiary());
        textTodo.setOnClickListener(null); // No action for the activated text
    }

    private void activateDiary() {
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        emptyTextView.setVisibility(View.GONE);
        todoContainer.setVisibility(View.GONE);
        addTodoButton.setVisibility(View.GONE);
        diaryEditText.setVisibility(View.VISIBLE);

        loadToDoList(selectedDate);

        textTodo.setOnClickListener(v -> activateTodo());
        textDiary.setOnClickListener(null); // No action for the activated text
    }

    private void loadToDoList(String date) {
        if (diaryEditText.getVisibility() == View.VISIBLE) {
            // Load diary content
            String diaryContent = SharedPreferencesHelper.loadDiaryContent(requireContext(), date);
            diaryEditText.setText(diaryContent);
        } else {
            // Load to-do list
            toDoList.clear();
            toDoList.addAll(SharedPreferencesHelper.loadToDoList(requireContext(), date));
            sortToDoList();  // Ensure the list is sorted before updating the UI
            updateToDoContainer();
        }
    }

    private void saveToDoList(String date) {
        if (diaryEditText.getVisibility() == View.VISIBLE) {
            // Save diary content
            String diaryContent = diaryEditText.getText().toString();
            SharedPreferencesHelper.saveDiaryContent(requireContext(), date, diaryContent);
        } else {
            // Save to-do list
            SharedPreferencesHelper.saveToDoList(requireContext(), date, toDoList);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(calendarView.getDate()));
    }

    private void showAddToDoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add To-Do");

        final EditText input = new EditText(requireContext());
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
                Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showModifyDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modify/Delete To-Do");

        final EditText input = new EditText(requireContext());
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
                Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
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
            if (todoContainer.getVisibility() == View.VISIBLE) {
                emptyTextView.setVisibility(View.VISIBLE);
            }
            isTodoEmpty = Boolean.TRUE;
        } else {
            emptyTextView.setVisibility(View.GONE);
            isTodoEmpty = Boolean.FALSE;
            for (int i = 0; i < toDoList.size(); i++) {
                View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_todo, todoContainer, false);
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
        toDoList.sort((o1, o2) -> {
            if (o1.isDone() == o2.isDone()) {
                // If both items are either done or not done, sort by timestamp (newer first)
                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
            } else {
                // Otherwise, sort by done status (not done items first)
                return Boolean.compare(o1.isDone(), o2.isDone());
            }
        });
    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            }
        }
    }

    private void loadContacts() {
        contacts.clear(); // 기존 연락처 목록을 초기화합니다.

        ContentResolver contentResolver = requireContext().getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (nameIndex != -1 && phoneIndex != -1) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(nameIndex);
                        String phoneNumber = cursor.getString(phoneIndex);
                        contacts.add(name + " (" + phoneNumber + ")");
                    }
                }
            } finally {
                cursor.close();
            }
        }


        adapter.notifyDataSetChanged(); // 어댑터에 데이터 변경을 알립니다.
    }

    private void setUpDiaryTextWatcher() {
        diaryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed while text is changing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveToDoList(selectedDate);
            }
        });
    }
}
