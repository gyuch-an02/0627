package com.example.hello_world;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder> {

    private final ArrayList<ToDoItem> toDoList;
    private final OnItemLongClickListener longClickListener;
    private final Handler handler = new Handler();

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public ToDoAdapter(ArrayList<ToDoItem> toDoList, OnItemLongClickListener longClickListener) {
        this.toDoList = toDoList;
        this.longClickListener = longClickListener;
        sortToDoList();
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new ToDoViewHolder(view, this, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        ToDoItem toDoItem = toDoList.get(position);
        holder.toDoTextView.setText(toDoItem.getTask());
        holder.toDoCheckBox.setChecked(toDoItem.isDone());
    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    public void sortToDoList() {
        Collections.sort(toDoList, new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem o1, ToDoItem o2) {
                if (o1.isDone() == o2.isDone()) {
                    // If both items are either done or not done, sort by timestamp (newer first)
                    return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                } else {
                    // Otherwise, sort by done status (not done items first)
                    return Boolean.compare(o1.isDone(), o2.isDone());
                }
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void addItem(ToDoItem item) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                toDoList.add(item);
                sortToDoList();
                notifyItemInserted(toDoList.indexOf(item));
            }
        });
    }

    public void updateItem(int position, String task) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToDoItem item = toDoList.get(position);
                item.setTask(task);
                sortToDoList();
                int newPosition = toDoList.indexOf(item);
                if (newPosition != position) {
                    notifyItemMoved(position, newPosition);
                }
                notifyItemChanged(newPosition);
            }
        });
    }

    public void removeItem(int position) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                toDoList.remove(position);
                notifyItemRemoved(position);
                sortToDoList();
            }
        });
    }

    public ToDoItem getItem(int position) {
        return toDoList.get(position);
    }

    public static class ToDoViewHolder extends RecyclerView.ViewHolder {

        TextView toDoTextView;
        CheckBox toDoCheckBox;

        public ToDoViewHolder(@NonNull View itemView, ToDoAdapter adapter, OnItemLongClickListener longClickListener) {
            super(itemView);
            toDoTextView = itemView.findViewById(R.id.todoTextView);
            toDoCheckBox = itemView.findViewById(R.id.todoCheckBox);

            toDoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ToDoItem toDoItem = adapter.toDoList.get(position);
                    toDoItem.setDone(isChecked);
                    adapter.sortToDoList();
                    int newPosition = adapter.toDoList.indexOf(toDoItem);
                    if (newPosition != position) {
                        adapter.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyItemMoved(position, newPosition);
                            }
                        });
                    }
                    adapter.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemChanged(newPosition);
                        }
                    });
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(position);
                }
                return true;
            });
        }
    }
}
