package com.example.DailyTag.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DailyTag.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.EntryViewHolder> {

    private List<Map.Entry<String, List<Map.Entry<String, String>>>> entriesByDate;

    public void setEntries(Map<String, List<Map.Entry<String, String>>> entriesByDate) {
        this.entriesByDate = new ArrayList<>(entriesByDate.entrySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        Map.Entry<String, List<Map.Entry<String, String>>> dateEntry = entriesByDate.get(position);
        holder.dateTextView.setText(dateEntry.getKey());

        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : dateEntry.getValue()) {
            content.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        holder.contentTextView.setText(content.toString());
    }

    @Override
    public int getItemCount() {
        return entriesByDate == null ? 0 : entriesByDate.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView contentTextView;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }
}
