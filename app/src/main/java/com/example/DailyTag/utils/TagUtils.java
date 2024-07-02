package com.example.DailyTag.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;

import com.example.DailyTag.R;
import com.example.DailyTag.contacts.ContactManager;

import java.util.Set;

public class TagUtils {

    public static void renewTagLayout(Context context, LifecycleOwner lifecycleOwner, TagViewModel tagViewModel, LinearLayout tagContainer, View.OnClickListener deleteClickListener) {
        tagContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        tagViewModel.getTagSet().observe(lifecycleOwner, tagSet -> {
            tagContainer.removeAllViews();
            LinearLayout currentLine = null;

            for (String tag : tagSet) {
                if (currentLine == null || currentLine.getChildCount() >= 3) {
                    currentLine = new LinearLayout(context);
                    currentLine.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    currentLine.setOrientation(LinearLayout.HORIZONTAL);
                    tagContainer.addView(currentLine);
                }

                View tagView = inflater.inflate(R.layout.item_tag, currentLine, false);
                TextView tagTextView = tagView.findViewById(R.id.tagTextView);
                tagTextView.setText(tag);
                currentLine.addView(tagView);

                tagView.findViewById(R.id.deleteButton).setOnClickListener(v -> {
                    tagViewModel.removeTag(tag);
                    deleteClickListener.onClick(v);
                });
            }

            // Add the "add tag" button to the last line
            addAddTagButton(context, tagContainer, tagViewModel, deleteClickListener);
        });
    }

    public static void addAddTagButton(Context context, LinearLayout tagContainer, TagViewModel tagViewModel, View.OnClickListener deleteClickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View addTagView = inflater.inflate(R.layout.item_add_tag, tagContainer, false);

        AutoCompleteTextView addTagTextView = addTagView.findViewById(R.id.addTagTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, ContactManager.getContactNames(context));
        Log.d("TagUtils", "Adapter set with " + adapter.getCount() + " items");
        addTagTextView.setAdapter(adapter);

        addTagTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTag = adapter.getItem(position);
            if (selectedTag != null && !selectedTag.isEmpty()) {
                tagViewModel.addTag(selectedTag);
            }
        });

        // Set a TextWatcher to handle manual text entry
        addTagTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Handle text changes if needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                String enteredTag = s.toString();
                if (enteredTag.length() > 1) { // Assuming the tag should have at least 2 characters
                    tagViewModel.addTag(enteredTag);
                }
            }
        });

        // Adjust dropdown width to match the tagContainer
        addTagTextView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int width = tagContainer.getWidth();
            addTagTextView.setDropDownWidth(width);
        });

        LinearLayout lastLine = (LinearLayout) tagContainer.getChildAt(tagContainer.getChildCount() - 1);
        if (lastLine == null || lastLine.getChildCount() >= 3) {
            lastLine = new LinearLayout(context);
            lastLine.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            lastLine.setOrientation(LinearLayout.HORIZONTAL);
            tagContainer.addView(lastLine);
        }
        lastLine.addView(addTagView);
    }
}
