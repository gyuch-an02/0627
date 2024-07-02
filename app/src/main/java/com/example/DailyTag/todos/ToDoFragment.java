package com.example.DailyTag.todos;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.DailyTag.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.DailyTag.contacts.ContactsFragment;
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver;
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager;
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar;
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter;
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class ToDoFragment extends Fragment {

    private SingleRowCalendar singleRowCalendar;
    private LinearLayout todoContainer;
    private FloatingActionButton addTodoButton;
    private TextView emptyTextView;
    private TextView textTodo;
    private TextView textDiary;
    private AutoCompleteTextView diaryAutoCompleteTextView;
    private AutoCompleteTextView todoAutoCompleteTextView;
    private List<String> contactNames;
    private TextView tvMonth;
    private TextView tvYear;
    private LinearLayout monthYear;
    private ArrayList<ToDoItem> toDoList;
    private boolean isDiaryActive;
    private String selectedDate;
    private Boolean isTodoEmpty;
    private Calendar calendar;
    private int currentMonth;
    private ArrayAdapter<String> contactNameAdapter;
    private List<String> todoTagList;
    private List<String> diaryTagList;
    private LinearLayout todoTagContainer;
    private LinearLayout diaryTagContainer;
    private List<String> tagList;
    private TextWatcher textWatcher;
    private Stack<String> undoStack;
    private Stack<String> redoStack;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("onCreateView", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        singleRowCalendar = view.findViewById(R.id.singleRowCalendar);
        todoContainer = view.findViewById(R.id.todoContainer);
        addTodoButton = view.findViewById(R.id.addTodoButton);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        textTodo = view.findViewById(R.id.text_todo);
        textDiary = view.findViewById(R.id.text_diary);
        tvMonth = view.findViewById(R.id.tv_month);
        tvYear = view.findViewById(R.id.tv_year);
        monthYear = view.findViewById(R.id.month_year);
        diaryAutoCompleteTextView = view.findViewById(R.id.diaryAutoCompleteTextView);
        contactNames = ContactsFragment.getContactNames();
        diaryTagContainer = view.findViewById(R.id.tagContainer);
        Button undoButton = view.findViewById(R.id.undoButton);
        Button redoButton = view.findViewById(R.id.redoButton);
        undoButton.setOnClickListener(v -> undoLastChange());
        redoButton.setOnClickListener(v -> redoLastChange());

        contactNames = ContactsFragment.getContactNames();  // 연락처 이름 가져오기

        contactNameAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, contactNames);

        isTodoEmpty = Boolean.TRUE;

        toDoList = new ArrayList<>();
        diaryTagList = new ArrayList<>();
        todoTagList = new ArrayList<>();
        tagList = new ArrayList<>();
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        selectedDate = getCurrentDate();
        loadToDoDiary(selectedDate);
        loadTagList(selectedDate);

        setUpSingleRowCalendar();
        setUpMonthYearDisplay();
        setUpMonthYearPicker();
        addTodoButton.setOnClickListener(v -> showAddToDoDialog());

        setUpToggle();
        setupAutoCompleteTextView(diaryAutoCompleteTextView, contactNameAdapter);

        return view;
    }

    private void setUpSingleRowCalendar() {
        calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);

        CalendarViewManager rowCalendarManager = new CalendarViewManager() {
            @Override
            public int setCalendarViewResourceId(int position, @NonNull Date date, boolean isSelected) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return isSelected ? R.layout.selected_calendar_item : R.layout.calendar_item;
            }

            @Override
            public void bindDataToCalendarView(SingleRowCalendarAdapter.CalendarViewHolder holder, @NonNull Date date, int position, boolean isSelected) {
                TextView tvDay = holder.itemView.findViewById(R.id.tv_day);
                TextView tvWeek = holder.itemView.findViewById(R.id.tv_week);
                tvDay.setText(getDayNumber(date));
                tvWeek.setText(getDay3LettersName(date));
            }

            private String getDayNumber(Date date) {
                SimpleDateFormat sdf = new SimpleDateFormat("d", Locale.getDefault());
                return sdf.format(date);
            }

            private String getDay3LettersName(Date date) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
                return sdf.format(date);
            }
        };

        CalendarChangesObserver rowCalendarChangesObserver = new CalendarChangesObserver() {
            @Override
            public void whenWeekMonthYearChanged(@NonNull String s, @NonNull String s1, @NonNull String s2, @NonNull String s3, @NonNull Date date) {

            }

            @Override
            public void whenSelectionRestored() {

            }

            @Override
            public void whenSelectionChanged(boolean isSelected, int position, @NonNull Date date) {
                Log.d("whenSelectionChanged", "Selection changed to " + position + " for " + date);
                if (isSelected) {
                    saveToDoDiary(selectedDate);
                    saveTagList(selectedDate);
                    selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                    loadToDoDiary(selectedDate);
                    loadTagList(selectedDate);
                }

                if (isDiaryActive) {
                    activateDiary();
                } else {
                    activateTodo();
                }
            }

            @Override
            public void whenCalendarScrolled(int dx, int dy) {
                // Handle calendar scrolled event if needed
            }

            @Override
            public void whenSelectionRefreshed() {
                // Handle selection refreshed event if needed
            }
        };

        CalendarSelectionManager rowSelectionManager = (position, date) -> true;

        singleRowCalendar.setCalendarViewManager(rowCalendarManager);
        singleRowCalendar.setCalendarChangesObserver(rowCalendarChangesObserver);
        singleRowCalendar.setCalendarSelectionManager(rowSelectionManager);
        singleRowCalendar.setDates(getFutureDatesOfCurrentMonth());
        singleRowCalendar.init();

        // Set initial selection to today or 1st of the month
        setInitialSelection();
    }

    private void setInitialSelection() {
        Calendar today = Calendar.getInstance();
        int targetDate;
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            // If the selected month and year are the same as the current month and year, set the date to today
            targetDate = today.get(Calendar.DAY_OF_MONTH);
        } else {
            // Otherwise, set the date to the 1st of the selected month
            targetDate = 1;
        }
        calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        singleRowCalendar.select(targetDate - 1);

        // Scroll to the selected date to make it visible
        singleRowCalendar.scrollToPosition(targetDate - 1);
    }

    private void setUpMonthYearDisplay() {
        updateMonthYearDisplay();
    }

    private void setUpMonthYearPicker() {
        View.OnClickListener listener = v -> {
            MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(requireContext(), (selectedMonth, selectedYear) -> {
                calendar.set(Calendar.MONTH, selectedMonth);
                calendar.set(Calendar.YEAR, selectedYear);
                updateCalendar();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

            builder.setActivatedMonth(calendar.get(Calendar.MONTH))
                    .setMinYear(1990)
                    .setActivatedYear(calendar.get(Calendar.YEAR))
                    .setMaxYear(2030)
                    .setTitle("Select Month and Year")
                    .build()
                    .show();
        };

        monthYear.setOnClickListener(listener);
    }

    private void updateMonthYearDisplay() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        String currentMonth = monthFormat.format(calendar.getTime());
        String currentYear = yearFormat.format(calendar.getTime());

        tvMonth.setText(currentMonth);
        tvYear.setText(currentYear);
    }

    private void updateCalendar() {
        updateMonthYearDisplay();
        singleRowCalendar.setDates(getFutureDatesOfCurrentMonth());
        singleRowCalendar.init();

        setInitialSelection();

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        loadToDoDiary(selectedDate);
    }

    private List<Date> getFutureDatesOfCurrentMonth() {
        currentMonth = calendar.get(Calendar.MONTH);
        return getDates(new ArrayList<>());
    }

    private List<Date> getDates(List<Date> list) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        list.add(calendar.getTime());
        while (currentMonth == calendar.get(Calendar.MONTH)) {
            calendar.add(Calendar.DATE, 1);
            if (currentMonth == calendar.get(Calendar.MONTH)) {
                list.add(calendar.getTime());
            }
        }
        calendar.add(Calendar.DATE, -1);
        return list;
    }

    private void setupAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView, ArrayAdapter<String> adapter) {
        setupAutoCompleteTextView(autoCompleteTextView, adapter, true, null);
    }

    private void setupAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView, ArrayAdapter<String> adapter, boolean isDiary, ToDoItem toDoItem) {
        autoCompleteTextView.setAdapter(adapter);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                undoStack.push(s.toString());
                redoStack.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.contains("@")) {
                    int fromIndex = input.lastIndexOf("@");
                    int toIndex = input.indexOf(' ', fromIndex);
                    String query = input.substring(fromIndex + 1, toIndex < 0 ? autoCompleteTextView.getText().length() : toIndex);
                    if (!query.isEmpty()) {
                        autoCompleteTextView.post(() -> adapter.getFilter().filter(query, resultCount -> {
                            if (resultCount > 0) {
                                autoCompleteTextView.showDropDown();
                            } else {
                                autoCompleteTextView.dismissDropDown();
                            }
                        }));
                    } else {
                        autoCompleteTextView.post(() -> {
                            adapter.getFilter().filter("");
                            autoCompleteTextView.showDropDown();
                        });
                    }
                } else {
                    autoCompleteTextView.post(autoCompleteTextView::dismissDropDown);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                saveToDoDiary(selectedDate);
            }
        };

        autoCompleteTextView.addTextChangedListener(textWatcher);

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTag = adapter.getItem(position);
            Log.d("OnItemClickListener", "Item selected: " + selectedTag);
            if (selectedTag != null) {
                Log.d("OnItemClickListener", "selectedTag is not null");
                int cursorPosition = autoCompleteTextView.getSelectionStart();
                Editable editable = autoCompleteTextView.getText();
                int fromIndex = editable.toString().lastIndexOf("@", cursorPosition - 1);

                if (fromIndex != -1) {
                    int toIndex = editable.toString().indexOf(' ', fromIndex);
                    if (toIndex == -1) {
                        toIndex = autoCompleteTextView.getText().length();
                    }
                    autoCompleteTextView.removeTextChangedListener(textWatcher);

                    String textBefore = editable.toString().substring(0, fromIndex);
                    String textAfter = editable.toString().substring(toIndex);

                    autoCompleteTextView.setText(textBefore + selectedTag + textAfter);
                    autoCompleteTextView.setSelection((textBefore + selectedTag).length());

                    autoCompleteTextView.addTextChangedListener(textWatcher);

                    if (isDiary) {
                        Log.d("OnItemClickListener", "Add diary tag " + selectedTag);
                        addTagToLayout(diaryTagContainer, diaryTagList, selectedTag);
                    } else if (toDoItem != null) {
                        Log.d("OnItemClickListener", "Add to-do tag " + selectedTag + " to " + toDoItem.getId());
                        toDoItem.getTags().add(selectedTag);
                        Log.d("OnItemClickListener", "Added: " + toDoItem.getTags());
                    }
                    else {
                        Log.d("OnItemClickListener", "isDiary: " + isDiary + " toDoItem: " + toDoItem);
                    }
                    saveToDoDiary(selectedDate);
                    saveTagList(selectedDate);
                }
                else {
                    Log.d("OnItemClickListener", "NOWAY!!!!");
                }
            }
        });

        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                autoCompleteTextView.dismissDropDown();
            } else {
                String input = autoCompleteTextView.getText().toString();
                if (input.contains("@")) {
                    int fromIndex = input.lastIndexOf("@");
                    int toIndex = input.indexOf(' ', fromIndex);
                    String query = input.substring(fromIndex + 1, toIndex < 0 ? autoCompleteTextView.getSelectionStart() : toIndex);
                    if (!query.isEmpty()) {
                        adapter.getFilter().filter(query, count -> {
                            if (count > 0) {
                                autoCompleteTextView.showDropDown();
                            } else {
                                autoCompleteTextView.dismissDropDown();
                            }
                        });
                    }
                }
            }
        });
    }

    private void undoLastChange() {
        if (!undoStack.isEmpty()) {
            // Get the last state from the stack
            String lastState = undoStack.pop();
            // Save the current state to the redo stack
            redoStack.push(diaryAutoCompleteTextView.getText().toString());

            // Temporarily remove the text watcher to avoid triggering it
            diaryAutoCompleteTextView.removeTextChangedListener(textWatcher);

            // Revert to the last state
            diaryAutoCompleteTextView.setText(lastState);

            // Move the cursor to the end of the text
            diaryAutoCompleteTextView.setSelection(lastState.length());

            // Re-attach the text watcher
            diaryAutoCompleteTextView.addTextChangedListener(textWatcher);

            // Save the reverted state
            saveToDoDiary(selectedDate);
        } else {
            Toast.makeText(requireContext(), "No more changes to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redoLastChange() {
        if (!redoStack.isEmpty()) {
            // Get the last undone state from the stack
            String lastState = redoStack.pop();
            // Save the current state to the undo stack
            undoStack.push(diaryAutoCompleteTextView.getText().toString());

            // Temporarily remove the text watcher to avoid triggering it
            diaryAutoCompleteTextView.removeTextChangedListener(textWatcher);

            // Restore the last undone state
            diaryAutoCompleteTextView.setText(lastState);

            // Move the cursor to the end of the text
            diaryAutoCompleteTextView.setSelection(lastState.length());

            // Re-attach the text watcher
            diaryAutoCompleteTextView.addTextChangedListener(textWatcher);

            // Save the restored state
            saveToDoDiary(selectedDate);
        } else {
            Toast.makeText(requireContext(), "No more changes to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTagToLayout(LinearLayout tagContainer, List<String> tagList, String tag) {
        // Check if the tag is already in the layout
        for (int i = 0; i < tagContainer.getChildCount(); i++) {
            View existingTagView = tagContainer.getChildAt(i);
            TextView existingTagTextView = existingTagView.findViewById(R.id.tagTextView);
            if (existingTagTextView.getText().toString().equals(tag)) {
                // Tag already exists in the layout, do not add it again
                Toast.makeText(requireContext(), "Tag already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Inflate the tag layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View tagView = inflater.inflate(R.layout.item_tag, tagContainer, false);

        // Set the tag text
        TextView tagTextView = tagView.findViewById(R.id.tagTextView);
        tagTextView.setText(tag);
//
//        // Set a random background color
//        int backgroundColor = Color.parseColor("#FF344C64");
//        GradientDrawable background = (GradientDrawable) tagView.getBackground();
//        background.setColor(backgroundColor);

        // Set the delete button behavior
        ImageButton deleteButton = tagView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            tagContainer.removeView(tagView);
            tagList.remove(tag);
            saveTagList(selectedDate);
            if (tagContainer.getChildCount() == 0) {
                tagContainer.setVisibility(View.GONE);
            }
        });

        // Add the tag view to the container
        tagContainer.addView(tagView);
        if (!tagList.contains(tag)) {
            tagList.add(tag);
            saveTagList(selectedDate);
        }

        // Show the tag container if hidden
        tagContainer.setVisibility(View.VISIBLE);
    }

    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    private void loadTagList(String date) {
        Log.d("loadTagList", "loading tag list for " + date);
        // Load diary tags for the selected date
        diaryTagList = new ArrayList<>(SharedPreferencesHelper.loadDiaryTags(requireContext(), date));

        // Load to-do tags for the selected date
        Map<String, Set<String>> todoTagsMap = SharedPreferencesHelper.loadToDoTags(requireContext(), date);

        // Assign loaded tags to the corresponding to-do items
        for (ToDoItem item : toDoList) {
            Set<String> tags = todoTagsMap.get(item.getId());
            if (tags != null) {
                item.setTags(new ArrayList<>(tags));
            }
        }

        // Update the tag containers
        updateTagContainer(diaryTagContainer, diaryTagList);
        updateToDoContainer(); // Refresh to-do items to show tags
    }


    private void saveTagList(String date) {
        Log.d("saveTagList", "Saving tag list for " + date);
        // Save diary tags for the selected date
        SharedPreferencesHelper.saveDiaryTags(requireContext(), date, new HashSet<>(diaryTagList));

        // Prepare the to-do tags map for saving
        Map<String, Set<String>> todoTagsMap = new HashMap<>();
        for (ToDoItem item : toDoList) {
            todoTagsMap.put(item.getId(), new HashSet<>(item.getTags()));
            Log.d("saveTagList", "Saved tags " + item.getTags().toString() + " onto id " + item.getId());
        }

        // Save to-do tags for the selected date
        SharedPreferencesHelper.saveToDoTags(requireContext(), date, todoTagsMap);
    }

    private void updateToDoContainer() {
        Log.d("updateToDoContainer", "Update To-do Container for " + selectedDate);
        todoContainer.removeAllViews();
        if (toDoList.isEmpty()) {
            if (todoContainer.getVisibility() == View.VISIBLE) {
                emptyTextView.setVisibility(View.VISIBLE);
            }
            isTodoEmpty = Boolean.TRUE;
        } else {
            emptyTextView.setVisibility(View.GONE);
            isTodoEmpty = Boolean.FALSE;
            for (int i = 0; i < toDoList.size(); i++) {
                View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_todo, todoContainer, false);
                TextView toDoTextView = itemView.findViewById(R.id.todoTextView);
                CheckBox toDoCheckBox = itemView.findViewById(R.id.todoCheckBox);
                todoTagContainer = itemView.findViewById(R.id.tagContainer); // tagContainer specific to each To-Do item

                ToDoItem toDoItem = toDoList.get(i);
                toDoTextView.setText(toDoItem.getTask());
                toDoCheckBox.setChecked(toDoItem.isDone());

                todoTagContainer.removeAllViews();
                if (!toDoItem.getTags().isEmpty()) {
                    todoTagContainer.setVisibility(View.VISIBLE);
                    for (String tag : toDoItem.getTags()) {
                        addTagToLayout(todoTagContainer, toDoItem.getTags(), tag); // Adding tags specific to each To-Do item
                    }
                } else {
                    todoTagContainer.setVisibility(View.GONE);
                    Log.d("updateToDoContainer", "No tags found");
                }

                final int position = i;
                toDoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toDoItem.setDone(isChecked);
                    sortToDoList();  // Sort after changing the done status
                    saveToDoDiary(selectedDate);
                    updateToDoContainer();
                });

                toDoTextView.setOnClickListener(v -> showModifyDeleteDialog(position));

                todoContainer.addView(itemView);
            }
        }
    }

    private void updateTagContainer(LinearLayout tagContainer, List<String> tagList) {
        tagContainer.removeAllViews();
        if (tagList.isEmpty()) {
            tagContainer.setVisibility(View.GONE);
        } else {
            tagContainer.setVisibility(View.VISIBLE);
            for (String tag : tagList) {
                addTagToLayout(tagContainer, tagList, tag);
            }
        }
    }

    private void setUpToggle() {
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        textDiary.setOnClickListener(v -> activateDiary());
        textTodo.setOnClickListener(v -> activateTodo());
    }

    private void activateTodo() {
        isDiaryActive = false;  // Update the state

        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        diaryAutoCompleteTextView.setVisibility(View.GONE);

        if (isTodoEmpty) {
            emptyTextView.setVisibility(View.VISIBLE);
        }
        todoContainer.setVisibility(View.VISIBLE);
        addTodoButton.setVisibility(View.VISIBLE);
        diaryAutoCompleteTextView.setVisibility(View.GONE);
        diaryTagContainer.setVisibility(View.GONE);  // 태그 컨테이너 숨기기

        textDiary.setOnClickListener(v -> activateDiary());
        textTodo.setOnClickListener(null); // No action for the activated text
    }

    private void activateDiary() {
        isDiaryActive = true;  // Update the state

        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        loadToDoDiary(selectedDate);

        emptyTextView.setVisibility(View.GONE);
        todoContainer.setVisibility(View.GONE);
        addTodoButton.setVisibility(View.GONE);
        diaryAutoCompleteTextView.setVisibility(View.VISIBLE);
        diaryTagContainer.setVisibility(View.VISIBLE);  // 태그 컨테이너 보이기

        textTodo.setOnClickListener(v -> activateTodo());
        textDiary.setOnClickListener(null); // No action for the activated text
    }

    private void loadToDoDiary(String date) {
        Log.d("loadToDoDiary", "load to-do and diary for " + date);
        // Load the to-do list for the specified date
        toDoList = SharedPreferencesHelper.loadToDoList(requireContext(), date);

        // Load the tags associated with the to-do items for the specified date
        Map<String, Set<String>> todoTagsMap = SharedPreferencesHelper.loadToDoTags(requireContext(), date);

        // Assign the loaded tags to the corresponding to-do items
        for (ToDoItem item : toDoList) {
            Set<String> tags = todoTagsMap.get(item.getId());
            if (tags != null) {
                item.setTags(new ArrayList<>(tags));
            }
        }

        // Load the diary content for the specified date
        String diaryContent = SharedPreferencesHelper.loadDiaryContent(requireContext(), date);
        diaryAutoCompleteTextView.setText(diaryContent);

        // Refresh the to-do container to reflect the loaded items and tags
        updateToDoContainer();
    }

    private void saveToDoDiary(String date) {
        if (diaryAutoCompleteTextView.getVisibility() == View.VISIBLE) {
            // Save diary content
            String diaryContent = diaryAutoCompleteTextView.getText().toString();
            SharedPreferencesHelper.saveDiaryContent(requireContext(), date, diaryContent);
            SharedPreferencesHelper.saveDiaryTags(requireContext(), date, new HashSet<>(diaryTagList));
        } else {
            // Save to-do list
            SharedPreferencesHelper.saveToDoList(requireContext(), date, toDoList);

            // Prepare the to-do tags map for saving
            Map<String, Set<String>> todoTagsMap = new HashMap<>();
            for (ToDoItem item : toDoList) {
                todoTagsMap.put(item.getId(), new HashSet<>(item.getTags()));
            }

            // Save to-do tags for the selected date
            SharedPreferencesHelper.saveToDoTags(requireContext(), date, todoTagsMap);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void showAddToDoDialog() {
        Log.d("showAddToDoDialog", "Adding to-do on " + selectedDate);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add To-Do");

        // Inflate the dialog's layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_todo, null);

        final CustomAutoCompleteTextView input = dialogView.findViewById(R.id.todoAutoCompleteTextView);
        input.setThreshold(1);  // Show suggestions after one character

        ToDoItem newItem = new ToDoItem("", false); // Create a new ToDoItem
        setupAutoCompleteTextView(input, contactNameAdapter, false, newItem); // Pass the new ToDoItem

        builder.setView(dialogView); // Set the view of the dialog here

        builder.setPositiveButton("Add", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                Log.d("showAddToDoDialog", "New item " + newItem + " added with tags " + newItem.getTags());
                newItem.setTask(task);
                updateTagList(newItem.getTags(), todoTagList); // Update tags for the new item
                toDoList.add(newItem);
                sortToDoList();  // Sort after adding a new item
                saveToDoDiary(selectedDate);
                saveTagList(selectedDate); // Save tags
                updateToDoContainer();
            } else {
                Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showModifyDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modify/Delete To-Do");

        // Inflate the dialog's layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_todo, null);
        builder.setView(dialogView);

        final CustomAutoCompleteTextView input = dialogView.findViewById(R.id.todoAutoCompleteTextView);
        input.setThreshold(1);  // Show suggestions after one character

        ToDoItem toDoItem = toDoList.get(position); // Get the specific ToDoItem
        setupAutoCompleteTextView(input, contactNameAdapter, false, toDoItem); // Pass the ToDoItem

        input.setText(toDoItem.getTask());

        builder.setPositiveButton("Modify", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                toDoItem.setTask(task);
                updateTagList(toDoItem.getTags(), todoTagList); // Update tags for the modified item
                sortToDoList();  // Sort after modifying an item
                saveToDoDiary(selectedDate);
                saveTagList(selectedDate); // Save tags
                updateToDoContainer();
            } else {
                Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            toDoList.remove(position);
            sortToDoList();  // Sort after deleting an item
            saveToDoDiary(selectedDate);
            saveTagList(selectedDate); // Save tags
            updateToDoContainer();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateTagList(List<String> itemTags, List<String> tagList) {
        for (String tag : itemTags) {
            if (!tagList.contains(tag)) {
                tagList.add(tag);
            }
        }
        saveToDoDiary(selectedDate); // Save the updated tags along with the to-do list
    }

    private void sortToDoList() {
        toDoList.sort((o1, o2) -> Boolean.compare(o1.isDone(), o2.isDone()));
    }
}
