package com.example.DailyTag.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DailyTag.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryEntriesAdapter extends RecyclerView.Adapter<DiaryEntriesAdapter.DiaryEntryViewHolder> {

    private List<Map.Entry<String, String>> diaryEntries = new ArrayList<>();

    @NonNull
    @Override
    public DiaryEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_diary_entry, parent, false);
        return new DiaryEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryEntryViewHolder holder, int position) {
        Map.Entry<String, String> diaryEntry = diaryEntries.get(position);
        holder.diaryContentTextView.setText(diaryEntry.getValue());
        holder.diaryDateTextView.setText(diaryEntry.getKey());
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }

    public void setDiaryEntries(List<Map.Entry<String, String>> diaryEntries) {
        this.diaryEntries = diaryEntries;
        notifyDataSetChanged();
    }

    static class DiaryEntryViewHolder extends RecyclerView.ViewHolder {
        TextView diaryContentTextView;
        TextView diaryDateTextView;

        DiaryEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            diaryContentTextView = itemView.findViewById(R.id.diaryEntryContentTextView);
            diaryDateTextView = itemView.findViewById(R.id.diaryEntryDateTextView);
        }
    }
}
