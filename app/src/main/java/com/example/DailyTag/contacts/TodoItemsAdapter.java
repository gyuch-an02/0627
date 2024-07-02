package com.example.DailyTag.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DailyTag.R;
import com.example.DailyTag.todos.ToDoItem;

import java.util.List;
import java.util.Map;

public class TodoItemsAdapter extends RecyclerView.Adapter<TodoItemsAdapter.TodoViewHolder> {

    private List<Map.Entry<String, ToDoItem>> todoItems;

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_entry, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Map.Entry<String, ToDoItem> entry = todoItems.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return todoItems != null ? todoItems.size() : 0;
    }

    public void setTodoItems(List<Map.Entry<String, ToDoItem>> todoItems) {
        this.todoItems = todoItems;
        notifyDataSetChanged();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        private final TextView todoDateTextView;
        private final TextView todoContentTextView;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todoDateTextView = itemView.findViewById(R.id.todoDateTextView);
            todoContentTextView = itemView.findViewById(R.id.todoContentTextView);
        }

        public void bind(Map.Entry<String, ToDoItem> entry) {
            todoDateTextView.setText(entry.getKey());
            todoContentTextView.setText(entry.getValue().getTask());
        }
    }
}
