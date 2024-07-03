package com.example.DailyTag.contacts;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DailyTag.R;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Map<String, List<EntryItem>> entriesByDate;
    private List<Map.Entry<String, EntryItem>> flattenedEntries;

    public void setEntries(Map<String, List<EntryItem>> entriesByDate) {
        this.entriesByDate = entriesByDate;
        this.flattenedEntries = flattenEntries(entriesByDate);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return flattenedEntries.get(position).getValue().getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == EntryItem.TYPE_DIARY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary_entry, parent, false);
            return new DiaryViewHolder(view);
        } else if (viewType == EntryItem.TYPE_TODO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_entry, parent, false);
            return new ToDoViewHolder(view);
        } else if (viewType == EntryItem.TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_entry, parent, false);
            return new ImageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Map.Entry<String, EntryItem> itemEntry = flattenedEntries.get(position);
        String date = itemEntry.getKey();
        EntryItem item = itemEntry.getValue();

        if (holder instanceof DiaryViewHolder) {
            ((DiaryViewHolder) holder).bind(item.getText(), date);
        } else if (holder instanceof ToDoViewHolder) {
            ((ToDoViewHolder) holder).bind(item.getText(), date);
        }  else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(item.getBitmap(), date);
        }
    }

    @Override
    public int getItemCount() {
        return flattenedEntries == null ? 0 : flattenedEntries.size();
    }

    private List<Map.Entry<String, EntryItem>> flattenEntries(Map<String, List<EntryItem>> map) {
        List<Map.Entry<String, EntryItem>> flattenedList = new ArrayList<>();
        for (Map.Entry<String, List<EntryItem>> entry : map.entrySet()) {
            String date = entry.getKey();
            for (EntryItem item : entry.getValue()) {
                flattenedList.add(new AbstractMap.SimpleEntry<>(date, item));
            }
        }
        return flattenedList;
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        private TextView diaryContentTextView;
        private TextView diaryDateTextView;

        public DiaryViewHolder(View itemView) {
            super(itemView);
            diaryContentTextView = itemView.findViewById(R.id.diaryContentTextView);
            diaryDateTextView = itemView.findViewById(R.id.diaryDateTextView);
        }

        public void bind(String text, String date) {
            String diaryText = "Diary: \n" + text;
            diaryContentTextView.setText(diaryText);
            diaryDateTextView.setText(date);
        }
    }

    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        private TextView toDoTextView;
        private TextView toDoDateTextView;

        public ToDoViewHolder(View itemView) {
            super(itemView);
            toDoTextView = itemView.findViewById(R.id.todoContentTextView);
            toDoDateTextView = itemView.findViewById(R.id.todoDateTextView);
        }

        public void bind(String text, String date) {
            String toDoText = "To-do: \n" + text;
            toDoTextView.setText(toDoText);
            toDoDateTextView.setText(date);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView imageDateTextView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageContentImageView);
            imageDateTextView = itemView.findViewById(R.id.imageDateTextView);

            // Adjust ImageView properties
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(0, 0, 0, 0);
        }

        public void bind(Bitmap bitmap, String date) {
            imageView.setImageBitmap(bitmap);
            imageDateTextView.setText(date);
        }
    }

}
