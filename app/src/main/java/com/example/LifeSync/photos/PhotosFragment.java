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
import android.widget.EditText;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

import com.example.LifeSync.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private FloatingActionButton fab;
    private Calendar startDate, endDate;

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

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> showDateRangeDialog()); //람다 표현식

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            loadPhotos();
        }

        return view;
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
                .setTitle("Select Date Range")
                .setPositiveButton("OK", (dialog, which) -> {
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
                .setNegativeButton("Cancel", null)
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