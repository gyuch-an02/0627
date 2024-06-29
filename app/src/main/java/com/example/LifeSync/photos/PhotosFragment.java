package com.example.LifeSync.photos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.DatePickerDialog;

import com.example.LifeSync.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class PhotosFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private GroupedPhotoAdapter groupedPhotoAdapter;
    private LinkedHashMap<String, List<String>> groupedPhotos;
    private Calendar startDate, endDate;

    private Button todayButton, last7DaysButton, last30DaysButton, allButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photos, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        // Use GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4); // 4 columns
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return groupedPhotoAdapter.getItemViewType(position) == PhotoItem.TYPE_HEADER ? layoutManager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);

        groupedPhotos = new LinkedHashMap<>();
        groupedPhotoAdapter = new GroupedPhotoAdapter(groupedPhotos, getChildFragmentManager());
        recyclerView.setAdapter(groupedPhotoAdapter);

        todayButton = view.findViewById(R.id.todayButton);
        last7DaysButton = view.findViewById(R.id.last7DaysButton);
        last30DaysButton = view.findViewById(R.id.last30DaysButton);
        allButton = view.findViewById(R.id.allButton);
        ImageButton searchButton = view.findViewById(R.id.searchButton);

        todayButton.setOnClickListener(v -> {
            filterByToday();
            setButtonColors(todayButton);
        });
        last7DaysButton.setOnClickListener(v -> {
            filterByLast7Days();
            setButtonColors(last7DaysButton);
        });
        last30DaysButton.setOnClickListener(v -> {
            filterByLast30Days();
            setButtonColors(last30DaysButton);
        });
        allButton.setOnClickListener(v -> {
            groupedPhotoAdapter.updateData(groupedPhotos);
            setButtonColors(allButton);
        });
        searchButton.setOnClickListener(v -> showDateRangeDialog()); //람다 표현

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            loadPhotos();
        }

        setButtonColors(allButton);

        return view;
    }

    private void setButtonColors(Button activeButton) {
        // 모든 버튼을 회색으로 설정
        todayButton.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        last7DaysButton.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        last30DaysButton.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        allButton.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        // 선택된 버튼을 검정색으로 설정
        activeButton.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
    }
    private void filterByToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        filterByDateRange(today.getTime(), today.getTime());
    }

    private void filterByLast7Days() {
        Calendar today = Calendar.getInstance();
        Calendar last7Days = Calendar.getInstance();
        last7Days.add(Calendar.DAY_OF_YEAR, -7);
        filterByDateRange(last7Days.getTime(), today.getTime());
    }

    private void filterByLast30Days() {
        Calendar today = Calendar.getInstance();
        Calendar last30Days = Calendar.getInstance();
        last30Days.add(Calendar.DAY_OF_YEAR, -30);
        filterByDateRange(last30Days.getTime(), today.getTime());
    }


    private void showDateRangeDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dateRangeView = inflater.inflate(R.layout.dialog_date_range_picker, null);

        EditText startDateEditText = dateRangeView.findViewById(R.id.startDateEditText);
        EditText endDateEditText = dateRangeView.findViewById(R.id.endDateEditText);

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText, true));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText, false));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dateRangeView)
                .setTitle("검색 필터")
                .setPositiveButton("확인", (dialog, which) -> {
                    String startDateString = startDateEditText.getText().toString();
                    String endDateString = endDateEditText.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    try {
                        Date startDate = sdf.parse(startDateString);
                        Date endDate = sdf.parse(endDateString);
                        filterByDateRange(startDate, endDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("취소", null)
                .setNeutralButton("초기화", (dialog, which) -> {
                    groupedPhotoAdapter.updateData(groupedPhotos);
                })
                .create()
                .show();
    }

    private void showDatePickerDialog(EditText editText, boolean isStartDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editText.setText(sdf.format(selectedDate.getTime()));
                    if (isStartDate) {
                        startDate = selectedDate;
                    } else {
                        endDate = selectedDate;
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void filterByDateRange(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        LinkedHashMap<String, List<String>> filteredPhotos = new LinkedHashMap<>();
        for (String date : groupedPhotos.keySet()) {
            try {
                Date photoDate = sdf.parse(date);
                if (photoDate != null && !photoDate.before(startDate) && !photoDate.after(endDate)) {
                    filteredPhotos.put(date, groupedPhotos.get(date));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        groupedPhotoAdapter.updateData(filteredPhotos);
    }


    private void loadPhotos() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_MODIFIED};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");

        if (cursor != null) {
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            while (cursor.moveToNext()) {
                String photoPath = cursor.getString(dataIndex);
                long dateModified = cursor.getLong(dateIndex) * 1000L;
                String date = dateFormat.format(new Date(dateModified));

                if (!groupedPhotos.containsKey(date)) {
                    groupedPhotos.put(date, new ArrayList<>());
                }
                groupedPhotos.get(date).add(photoPath);
            }
            cursor.close();
        }
        groupedPhotoAdapter.updateData(groupedPhotos);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPhotos();
            } else {
                Toast.makeText(getContext(), "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}