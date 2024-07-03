package com.example.DailyTag.photos;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DailyTag.R;
import com.example.DailyTag.utils.Tag;
import com.example.DailyTag.utils.TagViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PhotosFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private GroupedPhotoAdapter groupedPhotoAdapter;
    private LinkedHashMap<String, List<String>> groupedPhotos;
    private Map<String, Set<Tag>> photoTags; // Updated to use Tag class

    private Button todayButton, last7DaysButton, last30DaysButton, allButton;
    private LinearLayout dateRangeLayout;
    private EditText startDateEditText, endDateEditText;
    private long contactId = 1; // Replace with actual contactId
    private String contactName = "SampleContact"; // Replace with actual contactName
    private TagViewModel tagViewModel;

    public static String getImagePathByImageFileName(Context context, String imageFileName) {
        String imagePath = null;

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media.DISPLAY_NAME + " = ?";
        String[] selectionArgs = {imageFileName};

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
        }

        return imagePath;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photos, container, false);

        tagViewModel = new ViewModelProvider(this).get(TagViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        // Use GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 4); // 4 columns
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return groupedPhotoAdapter.getItemViewType(position) == PhotoItem.TYPE_HEADER ? layoutManager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);

        groupedPhotos = new LinkedHashMap<>();
        photoTags = new HashMap<>(); // Initialize the photo tags map
        groupedPhotoAdapter = new GroupedPhotoAdapter(groupedPhotos, contactId, contactName, getChildFragmentManager(), tagViewModel);
        groupedPhotoAdapter.setPhotoTags(photoTags); // Set photo tags
        recyclerView.setAdapter(groupedPhotoAdapter);

        todayButton = view.findViewById(R.id.todayButton);
        last7DaysButton = view.findViewById(R.id.last7DaysButton);
        last30DaysButton = view.findViewById(R.id.last30DaysButton);
        allButton = view.findViewById(R.id.allButton);
        ImageButton searchButton = view.findViewById(R.id.searchButton);
        dateRangeLayout = view.findViewById(R.id.dateRangeLayout);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        Button confirmButton = view.findViewById(R.id.confirmButton);

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
        searchButton.setOnClickListener(v -> toggleDateRangeLayout());

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText, true));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText, false));
        confirmButton.setOnClickListener(v -> {
            String startDateString = startDateEditText.getText().toString();
            String endDateString = endDateEditText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                Date startDate = sdf.parse(startDateString);
                Date endDate = sdf.parse(endDateString);
                filterByDateRange(startDate, endDate);
                dateRangeLayout.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            loadPhotos();
        }

        // 기본 전체 버튼 검정
        setButtonColors(allButton);

        return view;
    }

    private void toggleDateRangeLayout() {
        if (dateRangeLayout.getVisibility() == View.GONE) {
            dateRangeLayout.setVisibility(View.VISIBLE);
        } else {
            dateRangeLayout.setVisibility(View.GONE);
        }
    }

    private void setButtonColors(Button activeButton) {
        // 모든 버튼을 회색으로 설정
        todayButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        last7DaysButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        last30DaysButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        allButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));

        // 선택된 버튼을 검정색으로 설정
        activeButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
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

    private void showDatePickerDialog(EditText editText, boolean isStartDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editText.setText(sdf.format(selectedDate.getTime()));
                    if (isStartDate) {
                    } else {
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
        Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");

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

                // Load tags from TagViewModel
                tagViewModel.loadTags(photoPath).observe(getViewLifecycleOwner(), tags -> {
                    if (tags != null) {
                        photoTags.put(photoPath, new HashSet<>(tags));
                    }
                });
            }
            cursor.close();
        }
        groupedPhotoAdapter.updateData(groupedPhotos);
        groupedPhotoAdapter.setPhotoTags(photoTags); // Set photo tags in the adapter
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPhotos();
            } else {
                Toast.makeText(requireContext(), "저장소 읽기 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
