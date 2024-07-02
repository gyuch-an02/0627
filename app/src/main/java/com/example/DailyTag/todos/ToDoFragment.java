package com.example.DailyTag.todos;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.DailyTag.R;
import com.example.DailyTag.contacts.ContactsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver;
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager;
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar;
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter;

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
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToDoFragment extends Fragment {
    private static final int TAG_WIDTH_DP = 45;
    private SingleRowCalendar singleRowCalendar;
    private LinearLayout todoContainer;
    private FloatingActionButton addTodoButton;
    private TextView emptyTextView;
    private TextView textTodo;
    private TextView textDiary;
    private AutoCompleteTextView diaryAutoCompleteTextView;
    private LinearLayout undoRedoContainer;
    private TextView tvMonth;
    private TextView tvYear;
    private LinearLayout monthYear;
    private ArrayList<ToDoItem> toDoList;
    private boolean isDiaryActive;
    private String selectedDate;
    private Calendar calendar;
    private int currentMonth;
    private ArrayAdapter<String> contactNameAdapter;
    private Set<String> diaryTagList;
    private LinearLayout diaryTagContainer;
    private LinearLayout todoTagContainer;
    private TextWatcher textWatcher;
    private Stack<String> undoStack;
    private Stack<String> redoStack;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("onCreateView", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_todo, container, false);
        initializeViews(view);
        initializeVariables();

        setUpSingleRowCalendar();
        setUpMonthYearDisplay();
        setUpMonthYearPicker();
        setUpToggle();
        setupAutoCompleteTextView(diaryAutoCompleteTextView, contactNameAdapter);

        return view;
    }

    private void initializeViews(View view) {
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
        diaryTagContainer = view.findViewById(R.id.tagContainer);
        Button undoButton = view.findViewById(R.id.undoButton);
        Button redoButton = view.findViewById(R.id.redoButton);
        undoRedoContainer = view.findViewById(R.id.undoredo);
        undoButton.setOnClickListener(v -> undoLastChange());
        redoButton.setOnClickListener(v -> redoLastChange());

        addTodoButton.setOnClickListener(v -> showAddToDoDialog());
    }

    private void initializeVariables() {
        List<String> contactNames = ContactsFragment.getContactNames();
        contactNameAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, contactNames);
        toDoList = new ArrayList<>();
        diaryTagList = new HashSet<>();
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        selectedDate = getCurrentDate();
        loadToDoDiary(selectedDate);
        loadTagList(selectedDate);
    }

    private void setUpSingleRowCalendar() {
        calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);

        singleRowCalendar.setCalendarViewManager(new CalendarViewManager() {
            @Override
            public int setCalendarViewResourceId(int position, @NonNull Date date, boolean isSelected) {
                return isSelected ? R.layout.selected_calendar_item : R.layout.calendar_item;
            }

            @Override
            public void bindDataToCalendarView(@NonNull SingleRowCalendarAdapter.CalendarViewHolder holder, @NonNull Date date, int position, boolean isSelected) {
                TextView tvDay = holder.itemView.findViewById(R.id.tv_day);
                TextView tvWeek = holder.itemView.findViewById(R.id.tv_week);
                tvDay.setText(getDayNumber(date));
                tvWeek.setText(getDay3LettersName(date));
            }

            private String getDayNumber(Date date) {
                return new SimpleDateFormat("d", Locale.getDefault()).format(date);
            }

            private String getDay3LettersName(Date date) {
                return new SimpleDateFormat("EEE", Locale.getDefault()).format(date);
            }
        });

        singleRowCalendar.setCalendarChangesObserver(new CalendarChangesObserver() {
            @Override
            public void whenSelectionChanged(boolean isSelected, int position, @NonNull Date date) {
                Log.d("whenSelectionChanged", "Selection changed to " + position + " for " + date);
                if (isSelected) {
                    saveToDoDiary(selectedDate);
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
            public void whenWeekMonthYearChanged(@NonNull String s, @NonNull String s1, @NonNull String s2, @NonNull String s3, @NonNull Date date) {
                // Implement this method if needed
            }

            @Override
            public void whenSelectionRestored() {
                // Implement this method if needed
            }

            @Override
            public void whenCalendarScrolled(int dx, int dy) {
                // Handle calendar scrolled event if needed
            }

            @Override
            public void whenSelectionRefreshed() {
                // Handle selection refreshed event if needed
            }
        });

        singleRowCalendar.setCalendarSelectionManager((position, date) -> true);
        singleRowCalendar.setDates(getFutureDatesOfCurrentMonth());
        singleRowCalendar.init();
        setInitialSelection();
    }

    private void setInitialSelection() {
        Calendar today = Calendar.getInstance();
        int targetDate = (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                ? today.get(Calendar.DAY_OF_MONTH)
                : 1;
        calendar.set(Calendar.DAY_OF_MONTH, targetDate);
        singleRowCalendar.select(targetDate - 1);
        singleRowCalendar.scrollToPosition(targetDate - 1);
    }

    private void setUpMonthYearDisplay() {
        updateMonthYearDisplay();
    }

    private void setUpMonthYearPicker() {
        monthYear.setOnClickListener(v -> new MonthPickerDialog.Builder(requireContext(), (selectedMonth, selectedYear) -> {
            calendar.set(Calendar.MONTH, selectedMonth);
            calendar.set(Calendar.YEAR, selectedYear);
            updateCalendar();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
                .setActivatedMonth(calendar.get(Calendar.MONTH))
                .setMinYear(1990)
                .setActivatedYear(calendar.get(Calendar.YEAR))
                .setMaxYear(2030)
                .setTitle("Select Month and Year")
                .build()
                .show());
    }

    private void updateMonthYearDisplay() {
        tvMonth.setText(new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime()));
        tvYear.setText(new SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.getTime()));
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
        List<Date> list = new ArrayList<>();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        do {
            list.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        } while (currentMonth == calendar.get(Calendar.MONTH));
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
                handleAutoCompleteTextChanged(autoCompleteTextView, adapter, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //
            }
        };

        autoCompleteTextView.addTextChangedListener(textWatcher);

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> handleAutoCompleteItemClick(autoCompleteTextView, adapter.getItem(position), isDiary, toDoItem));
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> handleAutoCompleteFocusChange(autoCompleteTextView, adapter, hasFocus));
    }

    private void handleAutoCompleteTextChanged(AutoCompleteTextView autoCompleteTextView, ArrayAdapter<String> adapter, String input) {
        if (input.contains("@")) {
            int fromIndex = input.lastIndexOf("@");
            int toIndex = findNextWhitespaceIndex(input, fromIndex);
            String query = input.substring(fromIndex + 1, toIndex < 0 ? autoCompleteTextView.getText().length() : toIndex);
            if (!query.isEmpty()) {
                autoCompleteTextView.post(() -> adapter.getFilter().filter(query, count -> {
                    if (count > 0) {
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

    @SuppressLint("SetTextI18n")
    private void handleAutoCompleteItemClick(AutoCompleteTextView autoCompleteTextView, String selectedTag, boolean isDiary, ToDoItem toDoItem) {
        if (selectedTag != null) {
            int cursorPosition = autoCompleteTextView.getSelectionStart();
            Editable editable = autoCompleteTextView.getText();
            int fromIndex = editable.toString().lastIndexOf("@", cursorPosition - 1);

            if (fromIndex != -1) {
                int toIndex = findNextWhitespaceIndex(editable.toString(), fromIndex);
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
                    diaryTagList.add(selectedTag);
                    renewTagLayout(diaryTagContainer, diaryTagList);
                    diaryTagContainer.setVisibility(View.VISIBLE);
                } else if (toDoItem != null) {
                    toDoItem.getTags().add(selectedTag);
                }
            }
        }
    }

    private void handleAutoCompleteFocusChange(AutoCompleteTextView autoCompleteTextView, ArrayAdapter<String> adapter, boolean hasFocus) {
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
    }

    private int findNextWhitespaceIndex(String input, int fromIndex) {
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find(fromIndex)) {
            return matcher.start();
        }
        return -1; // No whitespace found
    }

    private void undoLastChange() {
        if (!undoStack.isEmpty()) {
            String lastState = undoStack.pop();
            redoStack.push(diaryAutoCompleteTextView.getText().toString());

            diaryAutoCompleteTextView.removeTextChangedListener(textWatcher);
            diaryAutoCompleteTextView.setText(lastState);
            diaryAutoCompleteTextView.setSelection(lastState.length());
            diaryAutoCompleteTextView.addTextChangedListener(textWatcher);
        } else {
            Toast.makeText(requireContext(), "No more changes to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redoLastChange() {
        if (!redoStack.isEmpty()) {
            String lastState = redoStack.pop();
            undoStack.push(diaryAutoCompleteTextView.getText().toString());

            diaryAutoCompleteTextView.removeTextChangedListener(textWatcher);
            diaryAutoCompleteTextView.setText(lastState);
            diaryAutoCompleteTextView.setSelection(lastState.length());
            diaryAutoCompleteTextView.addTextChangedListener(textWatcher);
        } else {
            Toast.makeText(requireContext(), "No more changes to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private void renewTagLayout(LinearLayout tagContainer, Set<String> tagSet) {
        tagContainer.removeAllViews();
        if (tagSet.isEmpty()) {
            Log.d("renewTagLayout", "TagList empty on " + selectedDate);
            tagContainer.setVisibility(View.GONE);
        } else {
            Log.d("renewTagLayout", "TagList not empty on " + selectedDate);
            tagContainer.setVisibility(View.VISIBLE);
            for (String tag : tagSet) {
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                View tagView = inflater.inflate(R.layout.item_tag, tagContainer, false);

                TextView tagTextView = tagView.findViewById(R.id.tagTextView);
                int tagWidthPx = (int) (TAG_WIDTH_DP * getResources().getDisplayMetrics().density + 0.5f); // Convert dp to px
                tagTextView.setText(truncateTextToFit(tag, tagTextView, tagWidthPx));

                // Find the last line or create a new one if necessary
                LinearLayout lastLine;
                if (tagContainer.getChildCount() == 0 || !canFitMoreTagsInLine((LinearLayout) tagContainer.getChildAt(tagContainer.getChildCount() - 1))) {
                    lastLine = new LinearLayout(requireContext());
                    lastLine.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    lastLine.setOrientation(LinearLayout.HORIZONTAL);
                    lastLine.setPadding(8, 8, 8, 8);
                    tagContainer.addView(lastLine);
                } else {
                    lastLine = (LinearLayout) tagContainer.getChildAt(tagContainer.getChildCount() - 1);
                }

                // Add the tag view to the last line
                lastLine.addView(tagView);

                // Set the delete button behavior
                ImageButton deleteButton = tagView.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener(v -> {
                    tagSet.remove(tag);
                    renewTagLayout(tagContainer, tagSet);
                });
            }
        }
    }

    private boolean canFitMoreTagsInLine(LinearLayout line) {
        return line.getChildCount() < 3;
    }

    private String truncateTextToFit(String text, TextView textView, int maxWidth) {
        TextPaint textPaint = textView.getPaint();
        int availableWidth = maxWidth - textView.getPaddingLeft() - textView.getPaddingRight();
        return TextUtils.ellipsize(text, textPaint, availableWidth, TextUtils.TruncateAt.END).toString();
    }

    private void loadTagList(String date) {
        Log.d("loadTagList", "Loading tag list of " + date);
        diaryTagList = new HashSet<>(SharedPreferencesHelper.loadDiaryTags(requireContext(), date));
        Log.d("loadTagList", "DiaryTagList of " + date + " is " + diaryTagList);
        Map<String, Set<String>> todoTagsMap = SharedPreferencesHelper.loadToDoTags(requireContext(), date);

        checkAndInitializeToDoList();

        for (ToDoItem item : toDoList) {
            Set<String> tags = todoTagsMap.get(item.getId());
            if (tags != null) {
                item.setTags(new HashSet<>(tags));
            }
        }

        updateTagContainer(diaryTagContainer, diaryTagList);
        updateToDoContainer();
    }

    private void updateToDoContainer() {
        Log.d("updateToDoContainer", "Update To-do container of " + selectedDate);
        todoContainer.removeAllViews();
        checkAndInitializeToDoList();
        if (toDoList.isEmpty()) {
            Log.d("updateToDoContainer", "toDoList is Empty on " + selectedDate);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            for (ToDoItem toDoItem : toDoList) {
                View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_todo, todoContainer, false);
                TextView toDoTextView = itemView.findViewById(R.id.todoTextView);
                CheckBox toDoCheckBox = itemView.findViewById(R.id.todoCheckBox);
                todoTagContainer = itemView.findViewById(R.id.tagContainer);

                toDoTextView.setText(toDoItem.getTask());
                toDoCheckBox.setChecked(toDoItem.isDone());

                todoTagContainer.removeAllViews();
                if (!toDoItem.getTags().isEmpty()) {//태그가 있으면
                    renewTagLayout(todoTagContainer, toDoItem.getTags());
                    todoTagContainer.setVisibility(View.VISIBLE);
                } else {
                    Log.d("updateToDoContainer", "Tag empty for " + toDoItem);
                    todoTagContainer.setVisibility(View.GONE);
                }

                toDoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toDoItem.setDone(isChecked);
                    sortToDoList();
                    updateToDoContainer();
                });

                toDoTextView.setOnClickListener(v -> showModifyDeleteDialog(toDoList.indexOf(toDoItem)));
                todoContainer.addView(itemView);
            }
        }
    }

    private void updateTagContainer(LinearLayout tagContainer, Set<String> tagSet) {
        tagContainer.removeAllViews();
        if (tagSet.isEmpty()) {
            tagContainer.setVisibility(View.GONE);
        } else {
            renewTagLayout(tagContainer, tagSet);
            tagContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setUpToggle() {
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        textDiary.setOnClickListener(v -> activateDiary());
        textTodo.setOnClickListener(v -> activateTodo());
    }

    private void activateTodo() {
        isDiaryActive = false;
        toggleViews(textTodo, textDiary, diaryAutoCompleteTextView, diaryTagContainer, todoContainer, addTodoButton, undoRedoContainer);
    }

    private void activateDiary() {
        isDiaryActive = true;
        toggleViews(textDiary, textTodo, diaryAutoCompleteTextView, diaryTagContainer, todoContainer, addTodoButton, undoRedoContainer);
    }

    private void toggleViews(TextView activeText, TextView inactiveText, AutoCompleteTextView diaryAutoCompleteTextView, LinearLayout diaryTagContainer, LinearLayout todoContainer, FloatingActionButton addTodoButton, LinearLayout undoRedoContainer) {
        activeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        inactiveText.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        emptyTextView.setVisibility(isDiaryActive || !toDoList.isEmpty() ? View.GONE : View.VISIBLE);
        todoContainer.setVisibility(isDiaryActive ? View.GONE : View.VISIBLE);
        addTodoButton.setVisibility(isDiaryActive ? View.GONE : View.VISIBLE);
        diaryAutoCompleteTextView.setVisibility(isDiaryActive ? View.VISIBLE : View.GONE);
        diaryTagContainer.setVisibility(isDiaryActive ? View.VISIBLE : View.GONE);
        undoRedoContainer.setVisibility(isDiaryActive ? View.VISIBLE : View.GONE);

        inactiveText.setOnClickListener(v -> {
            if (isDiaryActive) activateTodo();
            else activateDiary();
        });
        activeText.setOnClickListener(null);
    }
    
    private void loadToDoDiary(String date) {
        toDoList = SharedPreferencesHelper.loadToDoList(requireContext(), date);
        checkAndInitializeToDoList();

        Map<String, Set<String>> todoTagsMap = SharedPreferencesHelper.loadToDoTags(requireContext(), date);
        for (ToDoItem item : toDoList) {
            Set<String> tags = todoTagsMap.get(item.getId());
            if (tags != null) {
                item.setTags(new HashSet<>(tags));
            }
        }

        diaryAutoCompleteTextView.setText(SharedPreferencesHelper.loadDiaryContent(requireContext(), date));
        updateToDoContainer();
    }

    private void saveToDoDiary(String date) {
        SharedPreferencesHelper.saveDiaryContent(requireContext(), date, diaryAutoCompleteTextView.getText().toString());
        SharedPreferencesHelper.saveDiaryTags(requireContext(), date, new HashSet<>(diaryTagList));
        SharedPreferencesHelper.saveToDoList(requireContext(), date, toDoList);

        Map<String, Set<String>> todoTagsMap = new HashMap<>();
        for (ToDoItem item : toDoList) {
            todoTagsMap.put(item.getId(), new HashSet<>(item.getTags()));
        }
        SharedPreferencesHelper.saveToDoTags(requireContext(), date, todoTagsMap);
    }

    private void checkAndInitializeToDoList() {
        if (toDoList == null) {
            toDoList = new ArrayList<>();
        }
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void showAddToDoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add To-Do");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_todo, null);
        final CustomAutoCompleteTextView input = dialogView.findViewById(R.id.todoAutoCompleteTextView);
        input.setThreshold(1);

        ToDoItem newItem = new ToDoItem("", false);
        setupAutoCompleteTextView(input, contactNameAdapter, false, newItem);

        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                newItem.setTask(task);
                toDoList.add(newItem);
                sortToDoList();
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

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_todo, null);
        builder.setView(dialogView);

        final CustomAutoCompleteTextView input = dialogView.findViewById(R.id.todoAutoCompleteTextView);
        input.setThreshold(1);

        ToDoItem toDoItem = toDoList.get(position);
        setupAutoCompleteTextView(input, contactNameAdapter, false, toDoItem);
        input.setText(toDoItem.getTask());

        builder.setPositiveButton("Modify", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                toDoItem.setTask(task);
                sortToDoList();
                updateToDoContainer();
            } else {
                Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            toDoList.remove(position);
            sortToDoList();
            updateToDoContainer();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sortToDoList() {
        toDoList.sort((o1, o2) -> Boolean.compare(o1.isDone(), o2.isDone()));
    }
}
