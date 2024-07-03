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
import com.example.DailyTag.utils.ImageUtils;
import com.example.DailyTag.utils.Tag;
import com.example.DailyTag.utils.TagRepository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ContactDetailsActivity extends AppCompatActivity {

    private TextView detailsTitle;
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

        detailsTitle = findViewById(R.id.detailsTitle);
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        entriesRecyclerView = findViewById(R.id.entriesRecyclerView);

        // Get the contact ID from the intent
        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);

        if (contactId != -1) {
            loadContactDetails(contactId);
        }

        // Initialize adapter
        entriesAdapter = new EntriesAdapter();

        // Set up RecyclerView
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entriesRecyclerView.setAdapter(entriesAdapter);

        // Load entries
        if (contactName != null) {
            Log.d("loadEntries", "Load entries for " + contactName);
            try {
                loadEntries(this, contactId);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadContactDetails(long contactId) {
        Contact contact = new Contact(contactId, null, null, null);
        contact.loadContactDetails(this);

        // Set the contact details in the views
        if (contact.getProfileImage() != null) {
            profileImageView.setImageBitmap(ImageUtils.getRoundedBitmap(contact.getProfileImage()));
        } else {
            Bitmap defaultProfileImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_profile);
            profileImageView.setImageBitmap(ImageUtils.getRoundedBitmap(defaultProfileImage));
        }

        contactName = contact.getName(); // Store contact name
        nameTextView.setText(contact.getName());
        phoneTextView.setText(contact.getPhoneNumber());
        detailsTitle.setText("DailyTag: @" + contactName); // Update the title with contact name
    }

    private void loadEntries(Context context, long contactId) throws ParseException {
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
