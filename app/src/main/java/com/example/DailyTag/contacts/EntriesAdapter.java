package com.example.DailyTag.contacts;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DailyTag.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<EntryItem> entryItems = new ArrayList<>();
    private static final SimpleDateFormat DATE_PREV_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATE_CURR_FORMAT = new SimpleDateFormat("yyyy. M. d.", Locale.getDefault());

    @SuppressLint("NotifyDataSetChanged")
    public void setEntries(Map<String, List<EntryItem>> entriesByDate) throws ParseException {
        entryItems.clear();
        for (Map.Entry<String, List<EntryItem>> entry : entriesByDate.entrySet()) {
            String date = entry.getKey();
            // Format the date
            String formattedDate = DATE_CURR_FORMAT.format(Objects.requireNonNull(DATE_PREV_FORMAT.parse(date)));
            // Add a date item
            entryItems.add(new EntryItem(EntryItem.TYPE_DATE, formattedDate, true));
            boolean hasDiary = false;
            boolean hasTodo = false;
            for (EntryItem item : entry.getValue()) {
                if (item.getType() == EntryItem.TYPE_DIARY && !hasDiary) {
                    // Add a header item for "Diary" before the first diary entry
                    entryItems.add(new EntryItem(EntryItem.TYPE_HEADER, "Diary", true));
                    hasDiary = true;
                } else if (item.getType() == EntryItem.TYPE_TODO && !hasTodo) {
                    // Add a header item for "To-do" before the first to-do entry
                    entryItems.add(new EntryItem(EntryItem.TYPE_HEADER, "To-do", true));
                    hasTodo = true;
                }
                entryItems.add(item);
            }
            // Add a padding item at the bottom
            entryItems.add(new EntryItem(EntryItem.TYPE_PADDING, "", true));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return entryItems.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EntryItem.TYPE_DATE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_entry, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == EntryItem.TYPE_DIARY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary_entry, parent, false);
            return new DiaryViewHolder(view);
        } else if (viewType == EntryItem.TYPE_TODO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_entry, parent, false);
            return new ToDoViewHolder(view);
        } else if (viewType == EntryItem.TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_entry, parent, false);
            return new ImageViewHolder(view);
        } else if (viewType == EntryItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == EntryItem.TYPE_PADDING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_padding_entry, parent, false);
            return new PaddingViewHolder(view);
        } else {
            throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EntryItem entryItem = entryItems.get(position);
        if (holder instanceof DateViewHolder) {
            DateViewHolder dateViewHolder = (DateViewHolder) holder;
            dateViewHolder.dateTextView.setText(entryItem.getContent());
        } else if (holder instanceof DiaryViewHolder) {
            DiaryViewHolder diaryViewHolder = (DiaryViewHolder) holder;
            diaryViewHolder.diaryTextView.setText(entryItem.getContent());
        } else if (holder instanceof ToDoViewHolder) {
            ToDoViewHolder toDoViewHolder = (ToDoViewHolder) holder;
            toDoViewHolder.toDoTextView.setText(entryItem.getContent());
        } else if (holder instanceof ImageViewHolder) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            imageViewHolder.imageView.setImageBitmap(entryItem.getBitmap());
            // Set width to 50% of the parent width
            ViewGroup.LayoutParams layoutParams = imageViewHolder.imageView.getLayoutParams();
            layoutParams.width = (int) (imageViewHolder.itemView.getContext().getResources().getDisplayMetrics().widthPixels * 0.5);
            imageViewHolder.imageView.setLayoutParams(layoutParams);
            imageViewHolder.imageView.setAdjustViewBounds(true); // Adjust bounds to preserve aspect ratio

            // Add click listener to magnify the image
            imageViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                private boolean isMagnified = false;

                @Override
                public void onClick(View v) {
                    if (isMagnified) {
                        // Shrink image
                        ValueAnimator anim = ValueAnimator.ofInt(imageViewHolder.imageView.getMeasuredWidth(), (int) (imageViewHolder.itemView.getContext().getResources().getDisplayMetrics().widthPixels * 0.5));
                        anim.addUpdateListener(valueAnimator -> {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = imageViewHolder.imageView.getLayoutParams();
                            layoutParams.width = val;
                            imageViewHolder.imageView.setLayoutParams(layoutParams);
                        });
                        anim.setDuration(300);
                        anim.start();
                    } else {
                        // Magnify image
                        ValueAnimator anim = ValueAnimator.ofInt(imageViewHolder.imageView.getMeasuredWidth(), imageViewHolder.itemView.getContext().getResources().getDisplayMetrics().widthPixels);
                        anim.addUpdateListener(valueAnimator -> {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = imageViewHolder.imageView.getLayoutParams();
                            layoutParams.width = val;
                            imageViewHolder.imageView.setLayoutParams(layoutParams);
                        });
                        anim.setDuration(300);
                        anim.start();
                    }
                    isMagnified = !isMagnified;
                }
            });
        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.headerTextView.setText(entryItem.getContent());
        } else if (holder instanceof PaddingViewHolder) {
            // No additional binding needed for padding view holder
        }
    }

    @Override
    public int getItemCount() {
        return entryItems.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView diaryTextView;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            diaryTextView = itemView.findViewById(R.id.diaryTextView);
        }
    }

    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        TextView toDoTextView;

        public ToDoViewHolder(@NonNull View itemView) {
            super(itemView);
            toDoTextView = itemView.findViewById(R.id.toDoTextView);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
        }
    }

    static class PaddingViewHolder extends RecyclerView.ViewHolder {
        public PaddingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
