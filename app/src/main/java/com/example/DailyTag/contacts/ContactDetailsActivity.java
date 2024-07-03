package com.example.DailyTag.contacts;

import static com.example.DailyTag.photos.PhotosFragment.getImagePathByImageFileName;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
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
            Log.d("loadEntries", "Load entries for " + contactName);
            loadEntries(this, contactId);
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

    private void loadEntries(Context context, long contactId) {
        TagRepository tagRepository = TagRepository.getInstance((Application) context.getApplicationContext());
        Map<String, List<EntryItem>> entriesByDate = new TreeMap<>(Collections.reverseOrder());

        Map<String, ?> allEntries = tagRepository.getAllEntries();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().endsWith("_diary")) {
                String date = entry.getKey().substring(0, entry.getKey().indexOf('_'));
                String diaryContent = tagRepository.loadDiaryContent(date);
                Set<Tag> diaryTags = tagRepository.loadTags(date + "_diary");

                for (Tag tag : diaryTags) {
                    if (tag.getContactId() == contactId) {
                        if (!entriesByDate.containsKey(date)) {
                            entriesByDate.put(date, new ArrayList<>());
                        }
                        entriesByDate.get(date).add(new EntryItem(EntryItem.TYPE_DIARY, diaryContent));
                        break;
                    }
                }
            } else if (entry.getKey().contains("_todo_tag")) {
                String[] parts = entry.getKey().split("_");
                String date = parts[0];
                int index = Integer.parseInt(parts[1]);
                List<ToDoItem> todos = tagRepository.loadToDoList(date);
                if (index < todos.size()) {
                    ToDoItem todo = todos.get(index);
                    Set<Tag> todoTags = tagRepository.loadTags(entry.getKey());

                    for (Tag tag : todoTags) {
                        if (tag.getContactId() == contactId) {
                            if (!entriesByDate.containsKey(date)) {
                                entriesByDate.put(date, new ArrayList<>());
                            }
                            entriesByDate.get(date).add(new EntryItem(EntryItem.TYPE_TODO, todo.getTask()));
                            break;
                        }
                    }
                }
            } else if (entry.getKey().endsWith("_image")) {
                String[] parts = entry.getKey().split("_");
                String date = parts[parts.length - 2];
                StringBuilder imageFileNameBuilder = new StringBuilder();

                for (int i = 0; i < parts.length - 2; i++) {
                    if (i > 0) {
                        imageFileNameBuilder.append("_");
                    }
                    imageFileNameBuilder.append(parts[i]);
                }

                String imageFileName = imageFileNameBuilder.toString();
                String imagePath = getImagePathByImageFileName(context, imageFileName);
                Set<Tag> imageTags = tagRepository.loadTags(imageFileName + "_" + date + "_image");

                for (Tag tag : imageTags) {
                    if (tag.getContactId() == contactId) {
                        if (!entriesByDate.containsKey(date)) {
                            entriesByDate.put(date, new ArrayList<>());
                        }

                        if (imagePath != null) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            entriesByDate.get(date).add(new EntryItem(EntryItem.TYPE_IMAGE, bitmap));
                        }
                        break;
                    }
                }
            }
        }

        entriesAdapter.setEntries(entriesByDate);
    }
}