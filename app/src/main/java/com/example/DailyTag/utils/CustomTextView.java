package com.example.DailyTag.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class CustomTextView extends AppCompatTextView {

    private StaticLayout staticLayout;
    private TextPaint textPaint;

    public CustomTextView(Context context) {
        super(context);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new TextPaint(getPaint());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (staticLayout == null || isDirty()) {
            int width = getWidth() - getPaddingLeft() - getPaddingRight();
            staticLayout = new StaticLayout(getText(), textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
        }

        int height = getHeight();
        int totalTextHeight = staticLayout.getHeight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int textY = paddingTop + (height - paddingTop - paddingBottom - totalTextHeight) / 2;

        canvas.save();
        canvas.translate(getPaddingLeft(), textY + 4); // Increasing the adjustment to crop more
        staticLayout.draw(canvas);
        canvas.restore();
    }
}
