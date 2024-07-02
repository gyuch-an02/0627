package com.example.DailyTag.utils;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.DailyTag.todos.ToDoItem;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagViewModel extends AndroidViewModel {

    private final TagRepository tagRepository;
    private final MutableLiveData<Set<String>> tagSet;
    private final MutableLiveData<List<ToDoItem>> toDoList;
    private final MutableLiveData<String> diaryContent;

    public TagViewModel(@NonNull Application application) {
        super(application);
        tagRepository = TagRepository.getInstance(application);
        tagSet = new MutableLiveData<>(new HashSet<>());
        toDoList = new MutableLiveData<>();
        diaryContent = new MutableLiveData<>();
    }

    public LiveData<Set<String>> loadTags(String identifier) {
        MutableLiveData<Set<String>> tagsLiveData = new MutableLiveData<>();
        Set<String> tags = tagRepository.loadTags(identifier);
        tagsLiveData.setValue(tags);
        return tagsLiveData;
    }

    public void saveTags(String identifier, Set<String> tags) {
        tagRepository.saveTags(identifier, tags);
        setTags(tags);
    }

    public LiveData<Set<String>> getTagSet() {
        return tagSet;
    }

    public void setTags(Set<String> tags) {
        tagSet.setValue(tags);
    }

    public void addTag(String identifier, String tag) {
        Set<String> tags = loadTags(identifier).getValue();
        tags.add(tag);
        tagRepository.saveTags(identifier, tags);
    }

    public void removeTag(String identifier, String tag) {
        Set<String> tags = tagSet.getValue();
        if (tags != null) {
            tags.remove(tag);
            saveTags(identifier, tags);
        }
    }

    public LiveData<List<ToDoItem>> loadToDoList(String date) {
        MutableLiveData<List<ToDoItem>> toDoListLiveData = new MutableLiveData<>();
        List<ToDoItem> toDoItems = tagRepository.loadToDoList(date);
        toDoListLiveData.setValue(toDoItems);
        return toDoListLiveData;
    }

    public void saveToDoList(String date, List<ToDoItem> toDoList) {
        tagRepository.saveToDoList(date, toDoList);
        this.toDoList.setValue(toDoList); // Update the LiveData
    }

    public LiveData<List<ToDoItem>> getToDoList() {
        return toDoList;
    }

    public LiveData<String> loadDiaryContent(String date) {
        MutableLiveData<String> diaryLiveData = new MutableLiveData<>();
        String diaryContent = tagRepository.loadDiaryContent(date);
        diaryLiveData.setValue(diaryContent);
        return diaryLiveData;
    }

    public void saveDiaryContent(String date, String diaryContent) {
        tagRepository.saveDiaryContent(date, diaryContent);
        this.diaryContent.setValue(diaryContent); // Update the LiveData
    }
}
