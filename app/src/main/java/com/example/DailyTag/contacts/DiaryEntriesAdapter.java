package com.example.DailyTag.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DailyTag.R;

import java.util.List;
import java.util.Map;

public class DiaryEntriesAdapter extends RecyclerView.Adapter<DiaryEntriesAdapter.DiaryViewHolder> {

    private List<Map.Entry<String, String>> diaryEntries;

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        Map.Entry<String, String> entry = diaryEntries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return diaryEntries != null ? diaryEntries.size() : 0;
    }

    public void setDiaryEntries(List<Map.Entry<String, String>> diaryEntries) {
        this.diaryEntries = diaryEntries;
        notifyDataSetChanged();
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        private final TextView diaryDateTextView;
        private final TextView diaryContentTextView;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            diaryDateTextView = itemView.findViewById(R.id.diaryDateTextView);
            diaryContentTextView = itemView.findViewById(R.id.diaryContentTextView);
        }

        public void bind(Map.Entry<String, String> entry) {
            diaryDateTextView.setText(entry.getKey());
            diaryContentTextView.setText(entry.getValue());
        }
    }
}
