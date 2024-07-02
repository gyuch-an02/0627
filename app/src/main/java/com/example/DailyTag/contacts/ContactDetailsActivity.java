package com.example.DailyTag.contacts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DailyTag.R;
import com.example.DailyTag.todos.SharedPreferencesHelper;
import com.example.DailyTag.todos.ToDoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactDetailsActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private RecyclerView diaryEntriesRecyclerView;
    private RecyclerView todoItemsRecyclerView;
    private DiaryEntriesAdapter diaryEntriesAdapter;
    private TodoItemsAdapter todoItemsAdapter;
    private String contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        diaryEntriesRecyclerView = findViewById(R.id.diaryEntriesRecyclerView);
        todoItemsRecyclerView = findViewById(R.id.todoItemsRecyclerView);

        // Get the contact ID from the intent
        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);

        if (contactId != -1) {
            loadContactDetails(contactId);
        }

        // Set up RecyclerViews
        diaryEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        todoItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapters
        diaryEntriesAdapter = new DiaryEntriesAdapter();
        todoItemsAdapter = new TodoItemsAdapter();

        // Set adapters to RecyclerViews
        diaryEntriesRecyclerView.setAdapter(diaryEntriesAdapter);
        todoItemsRecyclerView.setAdapter(todoItemsAdapter);

        // Load diary entries and to-do items
        if (contactName != null) {
            loadDiaryEntries(this, contactName);
            loadTodoItems(this, contactName);
        }
    }

    private void loadContactDetails(long contactId) {
        Contact contact = new Contact(contactId, null, null, null);
        contact.loadContactDetails(this);

        // Set the contact details in the views
        if (contact.getProfileImage() != null) {
            profileImageView.setImageBitmap(contact.getProfileImage());
        }
        contactName = contact.getName(); // Store contact name
        nameTextView.setText(contact.getName());
        phoneTextView.setText(contact.getPhoneNumber());
    }

    private void loadDiaryEntries(Context context, String contactName) {
        Map<String, ?> allEntries = SharedPreferencesHelper.getAllEntries(context);
        Log.d("loadDiaryEntries", "allEntries: " + allEntries);
        List<String> diaryEntries = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("diary_")) {
                String date = entry.getKey().substring("diary_".length());
                String diaryContent = SharedPreferencesHelper.loadDiaryContent(context, date);
                Set<String> diaryTags = SharedPreferencesHelper.loadDiaryTags(context, date);

                if (diaryTags.contains(contactName)) {
                    diaryEntries.add(diaryContent);
                }
            }
        }

        diaryEntriesAdapter.setDiaryEntries(diaryEntries);
    }

    private void loadTodoItems(Context context, String contactName) {
        Map<String, ?> allEntries = SharedPreferencesHelper.getAllEntries(context);
        List<String> todoItems = new ArrayList<>();

        // 모든 항목을 반복문을 통해 탐색합니다.
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("todo_tags_")) {
                // 키에서 날짜 부분을 추출합니다.
                String date = entry.getKey().substring("todo_tags_".length());
                // 해당 날짜의 할 일 태그들을 불러옵니다.
                Map<String, Set<String>> todoTags = SharedPreferencesHelper.loadToDoTags(context, date);

                for (Map.Entry<String, Set<String>> todoEntry : todoTags.entrySet()) {
                    if (todoEntry.getValue().contains(contactName)) {
                        // 해당 날짜의 할 일 리스트를 불러옵니다.
                        List<ToDoItem> todos = SharedPreferencesHelper.loadToDoList(context, date);

                        for (ToDoItem todo : todos) {
                            if (todo.getId().equals(todoEntry.getKey())) {
                                todoItems.add(todo.getTask()); // 할 일 내용을 추가합니다.
                            }
                        }
                    }
                }
            }
        }

        todoItemsAdapter.setTodoItems(todoItems);
    }

}
