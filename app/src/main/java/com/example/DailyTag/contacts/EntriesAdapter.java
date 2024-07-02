package com.example.DailyTag.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DailyTag.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Map<String, List<EntryItem>> entriesByDate;

    public void setEntries(Map<String, List<EntryItem>> entriesByDate) {
        this.entriesByDate = entriesByDate;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // Implement logic to return correct view type based on EntryItem type
        // Assuming entriesByDate is flattened, so implement accordingly
        return 0;
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
            ImageView imageView = new ImageView(parent.getContext());
            return new ImageViewHolder(imageView);
        }
        // Other view types
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EntryItem item = getItem(position);
        if (holder instanceof DiaryViewHolder) {
            ((DiaryViewHolder) holder).bind(item.getText());
        } else if (holder instanceof ToDoViewHolder) {
            ((ToDoViewHolder) holder).bind(item.getText());
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(item.getImageView());
        }
        // Other view bindings
    }

    @Override
    public int getItemCount() {
        return entriesByDate == null ? 0 : flattenEntries(entriesByDate).size();
    }

    private EntryItem getItem(int position) {
        List<EntryItem> flattenedEntries = flattenEntries(entriesByDate);
        return flattenedEntries.get(position);
    }

    private List<EntryItem> flattenEntries(Map<String, List<EntryItem>> map) {
        List<EntryItem> flattenedList = new ArrayList<>();
        for (List<EntryItem> list : map.values()) {
            flattenedList.addAll(list);
        }
        return flattenedList;
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        private TextView diaryContentTextView;

        public DiaryViewHolder(View itemView) {
            super(itemView);
            diaryContentTextView = itemView.findViewById(R.id.diaryContentTextView);
        }

        public void bind(String text) {
            diaryContentTextView.setText(text);
        }
    }

    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        private TextView toDoTextView;

        public ToDoViewHolder(View itemView) {
            super(itemView);
            toDoTextView = itemView.findViewById(R.id.todoTextView);
        }

        public void bind(String text) {
            toDoTextView.setText(text);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ImageViewHolder(ImageView view) {
            super(view);
            this.imageView = view;
        }

        public void bind(ImageView imageView) {
            this.imageView.setImageDrawable(imageView.getDrawable());
        }
    }
}
