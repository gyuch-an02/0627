package com.example.DailyTag.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
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

    public static void renewTagLayout(Context context, LifecycleOwner lifecycleOwner, TagViewModel tagViewModel, LinearLayout tagContainer, String identifier, View.OnClickListener onClickListener) {
        tagContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);
        int tagCount = 0;
        LinearLayout currentLine = createNewLine(context, tagContainer);

        for (String tag : tagViewModel.getTagSet().getValue()) {
            if (tagCount == 3) {
                currentLine = createNewLine(context, tagContainer);
                tagCount = 0;
            }
            View tagView = inflater.inflate(R.layout.item_tag, currentLine, false);
            TextView tagTextView = tagView.findViewById(R.id.tagTextView);
            tagTextView.setText(tag);
            currentLine.addView(tagView);
            tagView.findViewById(R.id.deleteButton).setOnClickListener(v -> {
                tagViewModel.removeTag(identifier, tag);
                onClickListener.onClick(tagView);
            });
            tagCount++;
        }

        addAddTagButton(context, tagContainer, tagViewModel, identifier, onClickListener, lifecycleOwner);
    }

    private static LinearLayout createNewLine(Context context, LinearLayout tagContainer) {
        LinearLayout newLine = new LinearLayout(context);
        newLine.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        newLine.setOrientation(LinearLayout.HORIZONTAL);
        newLine.setPadding(8, 8, 8, 8);
        tagContainer.addView(newLine);
        return newLine;
    }

    public static void addAddTagButton(Context context, LinearLayout tagContainer, TagViewModel tagViewModel, String identifier, View.OnClickListener onClickListener, LifecycleOwner lifecycleOwner) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View addTagView = inflater.inflate(R.layout.item_add_tag, tagContainer, false);
        AutoCompleteTextView addTagTextView = addTagView.findViewById(R.id.addTagTextView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, ContactManager.getContactNames(context));
        addTagTextView.setAdapter(adapter);

        addTagTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTag = adapter.getItem(position);
            if (selectedTag != null && !selectedTag.isEmpty()) {
                tagViewModel.addTag(identifier, selectedTag);
                renewTagLayout(context, lifecycleOwner, tagViewModel, tagContainer, identifier, onClickListener);
            }
        });

        tagContainer.addView(addTagView);
    }
}
