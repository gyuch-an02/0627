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

public class TodoItemsAdapter extends RecyclerView.Adapter<TodoItemsAdapter.TodoItemViewHolder> {

    private List<Map.Entry<String, String>> todoItems = new ArrayList<>();

    @NonNull
    @Override
    public TodoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_person, parent, false);
        return new TodoItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoItemViewHolder holder, int position) {
        Map.Entry<String, String> todoItem = todoItems.get(position);
        holder.todoContentTextView.setText(todoItem.getValue());
        holder.todoDateTextView.setText(todoItem.getKey());
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public void setTodoItems(List<Map.Entry<String, String>> todoItems) {
        this.todoItems = todoItems;
        notifyDataSetChanged();
    }

    static class TodoItemViewHolder extends RecyclerView.ViewHolder {
        TextView todoContentTextView;
        TextView todoDateTextView;

        TodoItemViewHolder(@NonNull View itemView) {
            super(itemView);
            todoContentTextView = itemView.findViewById(R.id.todoItemContentTextView);
            todoDateTextView = itemView.findViewById(R.id.todoItemDateTextView);
        }
    }
}
