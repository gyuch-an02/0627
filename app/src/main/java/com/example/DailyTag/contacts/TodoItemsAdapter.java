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

public class TodoItemsAdapter extends RecyclerView.Adapter<TodoItemsAdapter.TodoItemViewHolder> {

    private List<String> todoItems = new ArrayList<>();

    @NonNull
    @Override
    public TodoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_person, parent, false);
        return new TodoItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoItemViewHolder holder, int position) {
        String todoItem = todoItems.get(position);
        holder.todoItemTextView.setText(todoItem);
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public void setTodoItems(List<String> todoItems) {
        this.todoItems = todoItems;
        notifyDataSetChanged();
    }

    static class TodoItemViewHolder extends RecyclerView.ViewHolder {
        TextView todoItemTextView;

        TodoItemViewHolder(@NonNull View itemView) {
            super(itemView);
            todoItemTextView = itemView.findViewById(R.id.todoItemTextView);
        }
    }
}
