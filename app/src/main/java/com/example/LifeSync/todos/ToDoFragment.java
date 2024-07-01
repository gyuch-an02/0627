package com.example.LifeSync.todos;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.LifeSync.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver;
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager;
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar;
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter;
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager;
import com.michalsvec.singlerowcalendar.utils.DateUtils;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ToDoFragment extends Fragment {

    private SingleRowCalendar singleRowCalendar;
    private LinearLayout todoContainer;
    private FloatingActionButton addTodoButton;
    private TextView emptyTextView;
    private TextView textTodo;
    private TextView textDiary;
    private EditText diaryEditText;
    private TextView tvMonth;
    private TextView tvYear;
    private LinearLayout monthYear;
    private ArrayList<ToDoItem> toDoList;
    private String selectedDate;
    private Boolean isTodoEmpty;
    private Calendar calendar;
    private int currentMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        diaryEditText = view.findViewById(R.id.diaryEditText);
        isTodoEmpty = Boolean.TRUE;

        toDoList = new ArrayList<>();

        selectedDate = getCurrentDate();
        loadToDoList(selectedDate);

        setUpSingleRowCalendar();
        setUpMonthYearDisplay();
        setUpMonthYearPicker();
        addTodoButton.setOnClickListener(v -> showAddToDoDialog());

        setUpToggle();
        setUpDiaryTextWatcher();

        return view;
    }

    private void setUpSingleRowCalendar() {
        calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);

        CalendarViewManager rowCalendarManager = new CalendarViewManager() {
            @Override
            public int setCalendarViewResourceId(int position, Date date, boolean isSelected) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return isSelected ? R.layout.selected_calendar_item : R.layout.calendar_item;
            }

            @Override
            public void bindDataToCalendarView(SingleRowCalendarAdapter.CalendarViewHolder holder, Date date, int position, boolean isSelected) {
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
            public void whenSelectionChanged(boolean isSelected, int position, Date date) {
                if (isSelected) {
                    saveToDoList(selectedDate);
                    selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                    loadToDoList(selectedDate);
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

        CalendarSelectionManager rowSelectionManager = new CalendarSelectionManager() {
            @Override
            public boolean canBeItemSelected(int position, Date date) {
                return true;
            }
        };

        singleRowCalendar.setCalendarViewManager(rowCalendarManager);
        singleRowCalendar.setCalendarChangesObserver(rowCalendarChangesObserver);
        singleRowCalendar.setCalendarSelectionManager(rowSelectionManager);
        singleRowCalendar.setDates(getFutureDatesOfCurrentMonth());
        singleRowCalendar.init();
        Log.d("ToDoFragment", "Hello");

        // Set initial selection to today or 1st of the month
        setInitialSelection();
    }

    private void setInitialSelection() {
        Calendar today = Calendar.getInstance();
        Date targetDate;
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            // If the selected month and year are the same as the current month and year, set the date to today
            targetDate = today.getTime();
            Log.d("ToDoFragment", "Setting initial selection to today: " + targetDate.toString());
            calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            singleRowCalendar.select(today.get(Calendar.DAY_OF_MONTH)-1);
        } else {
            // Otherwise, set the date to the 1st of the selected month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            singleRowCalendar.select(0);
            targetDate = calendar.getTime();
            Log.d("ToDoFragment", "Setting initial selection to 1st of the month: " + targetDate.toString());
        }
    }

    private void setUpMonthYearDisplay() {
        updateMonthYearDisplay();
    }

    private void setUpMonthYearPicker() {
        View.OnClickListener listener = v -> {
            MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getContext(), (selectedMonth, selectedYear) -> {
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
        Calendar today = Calendar.getInstance();

        updateMonthYearDisplay();
        singleRowCalendar.setDates(getFutureDatesOfCurrentMonth());
        singleRowCalendar.init();

        setInitialSelection();

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        loadToDoList(selectedDate);
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

    private void setUpToggle() {
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        textDiary.setOnClickListener(v -> activateDiary());
        textTodo.setOnClickListener(v -> activateTodo());
    }

    private void activateTodo() {
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        diaryEditText.setVisibility(View.GONE);
        if (isTodoEmpty) {
            emptyTextView.setVisibility(View.VISIBLE);
        }
        todoContainer.setVisibility(View.VISIBLE);
        addTodoButton.setVisibility(View.VISIBLE);

        textDiary.setOnClickListener(v -> activateDiary());
        textTodo.setOnClickListener(null); // No action for the activated text
    }

    private void activateDiary() {
        textDiary.setTextColor(ContextCompat.getColor(requireContext(), R.color.active_text));
        textTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_text));

        emptyTextView.setVisibility(View.GONE);
        todoContainer.setVisibility(View.GONE);
        addTodoButton.setVisibility(View.GONE);
        diaryEditText.setVisibility(View.VISIBLE);

        loadToDoList(selectedDate);

        textTodo.setOnClickListener(v -> activateTodo());
        textDiary.setOnClickListener(null); // No action for the activated text
    }

    private void loadToDoList(String date) {
        if (diaryEditText.getVisibility() == View.VISIBLE) {
            // Load diary content
            String diaryContent = SharedPreferencesHelper.loadDiaryContent(requireContext(), date);
            diaryEditText.setText(diaryContent);
        } else {
            // Load to-do list
            toDoList.clear();
            toDoList.addAll(SharedPreferencesHelper.loadToDoList(requireContext(), date));
            sortToDoList();  // Ensure the list is sorted before updating the UI
            updateToDoContainer();
        }
    }

    private void saveToDoList(String date) {
        if (diaryEditText.getVisibility() == View.VISIBLE) {
            // Save diary content
            String diaryContent = diaryEditText.getText().toString();
            SharedPreferencesHelper.saveDiaryContent(requireContext(), date, diaryContent);
        } else {
            // Save to-do list
            SharedPreferencesHelper.saveToDoList(requireContext(), date, toDoList);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void showAddToDoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add To-Do");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                ToDoItem newItem = new ToDoItem(task, false);
                toDoList.add(newItem);
                sortToDoList();  // Sort after adding a new item
                saveToDoList(selectedDate);
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

        final EditText input = new EditText(requireContext());
        input.setText(toDoList.get(position).getTask());
        builder.setView(input);

        builder.setPositiveButton("Modify", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.isEmpty()) {
                toDoList.get(position).setTask(task);
                sortToDoList();  // Sort after modifying an item
                saveToDoList(selectedDate);
                updateToDoContainer();
            } else {
                Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            toDoList.remove(position);
            sortToDoList();  // Sort after deleting an item
            saveToDoList(selectedDate);
            updateToDoContainer();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateToDoContainer() {
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

                ToDoItem toDoItem = toDoList.get(i);
                toDoTextView.setText(toDoItem.getTask());
                toDoCheckBox.setChecked(toDoItem.isDone());

                final int position = i;
                toDoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toDoItem.setDone(isChecked);
                    sortToDoList();  // Sort after changing the done status
                    saveToDoList(selectedDate);
                    updateToDoContainer();
                });

                itemView.setOnLongClickListener(v -> {
                    showModifyDeleteDialog(position);
                    return true;
                });

                todoContainer.addView(itemView);
            }
        }
    }

    private void sortToDoList() {
        toDoList.sort((o1, o2) -> {
            if (o1.isDone() == o2.isDone()) {
                // If both items are either done or not done, sort by timestamp (newer first)
                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
            } else {
                // Otherwise, sort by done status (not done items first)
                return Boolean.compare(o1.isDone(), o2.isDone());
            }
        });
    }

    private void setUpDiaryTextWatcher() {
        diaryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed while text is changing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveToDoList(selectedDate);
            }
        });
    }

    private List<String> getContactNames() {
        List<String> contactList = new ArrayList<>();
        Cursor cursor = requireActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contactList.add(name);
            }
            cursor.close();
        }
        return contactList;
    }
}
