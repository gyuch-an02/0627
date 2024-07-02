package com.example.DailyTag.todos;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class DiaryAutoCompleteTextView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {
    public DiaryAutoCompleteTextView(Context context) {
        super(context);
    }

    public DiaryAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DiaryAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void replaceText(CharSequence text) {
        // do nothing so that the text stays the same
    }
}