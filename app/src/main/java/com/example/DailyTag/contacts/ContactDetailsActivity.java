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
import com.example.DailyTag.utils.Tag;
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
        Map<String, List<Map.Entry<String, String>>> entriesByDate = new TreeMap<>(Collections.reverseOrder());
        long contactId = ContactManager.getContactIdByName(context, contactName);

        // Load diary entries
        Map<String, ?> allEntries = tagRepository.getAllEntries();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().endsWith("_diary")) {
                String date = entry.getKey().substring(0, entry.getKey().indexOf('_'));
                String diaryContent = tagRepository.loadDiaryContent(date);
                Set<Tag> diaryTags = tagRepository.loadTags(date + "_diary");

                for (Tag tag : diaryTags) {
                    if (tag.getContactName().equals(contactName)) {
                        if (!entriesByDate.containsKey(date)) {
                            entriesByDate.put(date, new ArrayList<>());
                        }
                        entriesByDate.get(date).add(new AbstractMap.SimpleEntry<>("Diary", diaryContent));
                        break;
                    }
                }
            } else if (entry.getKey().endsWith("_todo")) {
                String date = entry.getKey().substring(0, entry.getKey().indexOf('_'));
                List<ToDoItem> todos = tagRepository.loadToDoList(date);
                Set<Tag> todoTags = tagRepository.loadTags(entry.getKey());

                for (ToDoItem todo : todos) {
                    for (Tag tag : todoTags) {
                        if (tag.getContactId() == contactId && todo.getId().equals(tag.getTagName())) {
                            if (!entriesByDate.containsKey(date)) {
                                entriesByDate.put(date, new ArrayList<>());
                            }
                            entriesByDate.get(date).add(new AbstractMap.SimpleEntry<>("To-Do", todo.getTask()));
                            break;
                        }
                    }
                }
            }
        }

        entriesAdapter.setEntries(entriesByDate);
    }
}
