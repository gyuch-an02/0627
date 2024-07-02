package com.example.DailyTag.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.DailyTag.R;
import com.example.DailyTag.contacts.ContactManager;

import java.util.Set;

public class TagUtils {

    public static void renewTagLayout(Context context, LinearLayout tagContainer, Set<String> tagSet, View.OnClickListener onClickListener) {
        tagContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (String tag : tagSet) {
            View tagView = inflater.inflate(R.layout.item_tag, tagContainer, false);
            TextView tagTextView = tagView.findViewById(R.id.tagTextView);
            tagTextView.setText(tag);
            tagContainer.addView(tagView);

            tagView.findViewById(R.id.deleteButton).setOnClickListener(onClickListener);
        }

        // Add the "add tag" button
        addAddTagButton(context, tagContainer, tagSet, onClickListener);
    }

    public static void addAddTagButton(Context context, LinearLayout tagContainer, Set<String> tagSet, View.OnClickListener onClickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View addTagView = inflater.inflate(R.layout.item_add_tag, tagContainer, false);

        AutoCompleteTextView addTagTextView = addTagView.findViewById(R.id.addTagTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, ContactManager.getContactNames(context));
        Log.d("TagUtils", "Adapter set with " + adapter.getCount() + " items");
        addTagTextView.setAdapter(adapter);

        addTagTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTag = adapter.getItem(position);
            if (selectedTag != null && !selectedTag.isEmpty()) {
                tagSet.add(selectedTag);
                renewTagLayout(context, tagContainer, tagSet, onClickListener);
            }
        });

        tagContainer.addView(addTagView);
    }
}
