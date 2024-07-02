package com.example.DailyTag.todos;

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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.DailyTag.R;
import com.example.DailyTag.utils.TagUtils;
import com.example.DailyTag.utils.TagManager;
import com.example.DailyTag.utils.TagViewModel;
import com.example.DailyTag.contacts.ContactManager;
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
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToDoFragment extends Fragment {
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
    private TagViewModel tagViewModel;
    private LinearLayout diaryTagContainer;
    private TextWatcher textWatcher;
    private Stack<String> undoStack;
    private Stack<String> redoStack;
    private TagManager tagManager;

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
        diaryTagContainer = view.findViewById(R.id.diaryTagContainer);
        Button undoButton = view.findViewById(R.id.undoButton);
        Button redoButton = view.findViewById(R.id.redoButton);
        undoRedoContainer = view.findViewById(R.id.undoredo);
        undoButton.setOnClickListener(v -> undoLastChange());
        redoButton.setOnClickListener(v -> redoLastChange());

        addTodoButton.setOnClickListener(v -> showAddToDoDialog());
    }

    private void initializeVariables() {
        List<String> contactNames = ContactManager.getContactNames(requireContext());
        contactNameAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, contactNames);
        toDoList = new ArrayList<>();
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        selectedDate = getCurrentDate();
        tagViewModel = new ViewModelProvider(this).get(TagViewModel.class);
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
                    updateToDoContainer();
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
                    Log.d("handleAutoCompleteItemClick", "is Diary!!");
                    tagViewModel.addTag(selectedDate + "_diary", selectedTag);
                    TagUtils.renewTagLayout(requireContext(), getViewLifecycleOwner(), tagViewModel, diaryTagContainer, selectedDate + "_diary", v -> {
                        String tagToRemove = ((TextView) v.findViewById(R.id.tagTextView)).getText().toString();
                        tagViewModel.removeTag(selectedDate + "_diary", tagToRemove);
                    });
                    diaryTagContainer.setVisibility(View.VISIBLE);
                } else if (toDoItem != null) {
                    tagViewModel.addTag(toDoItem.getId(), selectedTag);
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

    private void renewTagLayout(LinearLayout tagContainer, String identifier) {
        tagViewModel.getTagSet().observe(getViewLifecycleOwner(), tags -> {
            TagUtils.renewTagLayout(requireContext(), getViewLifecycleOwner(), tagViewModel, tagContainer, identifier, v -> {
                String tag = ((TextView) v.findViewById(R.id.tagTextView)).getText().toString();
                tagViewModel.removeTag(identifier, tag);
                renewTagLayout(tagContainer, identifier);
            });
        });
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
                LinearLayout todoTagContainer = itemView.findViewById(R.id.todoTagContainer);

                toDoTextView.setText(toDoItem.getTask());
                toDoCheckBox.setChecked(toDoItem.isDone());

                renewTagLayout(todoTagContainer, toDoItem.getId());

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

    private void loadTagList(String date) {
        tagViewModel.loadTags(date + "_diary").observe(getViewLifecycleOwner(), tagSet -> {
            renewTagLayout(diaryTagContainer, date + "_diary");
        });

        checkAndInitializeToDoList();
    }

    private void loadToDoDiary(String date) {
        tagViewModel.loadToDoList(date).observe(getViewLifecycleOwner(), toDoItems -> {
            if (toDoItems != null) {
                this.toDoList = new ArrayList<>(toDoItems);
            }
        });

        tagViewModel.loadDiaryContent(date).observe(getViewLifecycleOwner(), content -> {
            diaryAutoCompleteTextView.setText(content != null ? content : "");
        });

        tagViewModel.loadTags(date + "_diary").observe(getViewLifecycleOwner(), tags -> {
            tagViewModel.setTags(tags);  // Sync the tags
            renewTagLayout(diaryTagContainer, date + "_diary"); // Load tags for the diary
        });
    }

    private void saveToDoDiary(String date) {
        String diaryIdentifier = date + "_diary";
        tagViewModel.saveDiaryContent(date, diaryAutoCompleteTextView.getText().toString());
        tagViewModel.saveTags(diaryIdentifier, new HashSet<>(tagViewModel.loadTags(diaryIdentifier).getValue()));
        tagViewModel.saveToDoList(date, toDoList);

        for (ToDoItem item : toDoList) {
            tagViewModel.saveTags(item.getId(), new HashSet<>(tagViewModel.loadTags(item.getId()).getValue()));
        }
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
