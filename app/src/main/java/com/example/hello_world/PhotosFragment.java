package com.example.hello_world;

import android.Manifest;
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
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class PhotosFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private GroupedPhotoAdapter groupedPhotoAdapter;
    private LinkedHashMap<String, List<String>> groupedPhotos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photos, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        // Use GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3); // 3 columns
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

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            loadPhotos();
        }

        return view;
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
