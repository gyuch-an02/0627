package com.example.DailyTag.contacts;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DailyTag.R;
import com.example.DailyTag.todos.ToDoItem;
import com.example.DailyTag.utils.TagRepository;

import java.util.*;

public class ContactDetailsActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private RecyclerView entriesRecyclerView;
    private EntriesAdapter entriesAdapter;
    private String contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        entriesRecyclerView = findViewById(R.id.entriesRecyclerView);

        // Get the contact ID from the intent
        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);

        if (contactId != -1) {
            loadContactDetails(contactId);
        }

        // Set up RecyclerView
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter
        entriesAdapter = new EntriesAdapter();

        // Set adapter to RecyclerView
        entriesRecyclerView.setAdapter(entriesAdapter);

        // Load entries
        if (contactName != null) {
            loadEntries(this, contactName);
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

    private void loadEntries(Context context, String contactName) {
        TagRepository tagRepository = TagRepository.getInstance((Application) context.getApplicationContext());
        Map<String, List<Map.Entry<String, String>>> entriesByDate = new TreeMap<>();

        // Load diary entries
        Map<String, ?> allEntries = tagRepository.getAllEntries(); // Assuming this method exists to get all entries
        Log.d("loadEntries","allEntries : "+  allEntries);
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("loadEntries","entry.getKey() : "+entry.getKey());
            Log.d("loadEntries", "entry.getValue :" + entry.getValue());
            if (entry.getKey().endsWith("_diary")) { //다이어리 태그
                String date = entry.getKey().substring(0, entry.getKey().indexOf('_'));
                String diaryContent = tagRepository.loadDiaryContent(date);
                Set<String> diaryTags = tagRepository.loadTags(date + "_diary");

                if (diaryTags.contains(contactName)) {
                    if (!entriesByDate.containsKey(date)) {
                        entriesByDate.put(date, new ArrayList<>());
                    }
                    entriesByDate.get(date).add(new AbstractMap.SimpleEntry<>("Diary", diaryContent));
                }
            } else { //todo : 수정 필요
//                Set<String> todoTags = tagRepository.loadTags(entry.getKey());
//                for (String todoId : todoTags) {
//                    if (todoTags.contains(contactName)) {
//                        List<ToDoItem> todos = tagRepository.loadToDoList(date);
//
//                        for (ToDoItem todo : todos) {
//                            if (todo.getId().equals(todoId)) {
//                                if (!entriesByDate.containsKey(date)) {
//                                    entriesByDate.put(date, new ArrayList<>());
//                                }
//                                entriesByDate.get(date).add(new AbstractMap.SimpleEntry<>("To-Do", todo.getTask()));
//                            }
//                        }
//                    }
//                }

            }
        }

        entriesAdapter.setEntries(entriesByDate);
    }
}
