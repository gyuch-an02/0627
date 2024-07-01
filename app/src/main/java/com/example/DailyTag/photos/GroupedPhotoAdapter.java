package com.example.DailyTag.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DailyTag.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupedPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PhotoItem> itemList;
    private FragmentManager fragmentManager;

    public GroupedPhotoAdapter(Map<String, List<String>> groupedPhotos, FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        updateData(groupedPhotos);
    }

    public void updateData(Map<String, List<String>> groupedPhotos) {
        itemList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groupedPhotos.entrySet()) {
            itemList.add(new PhotoItem(entry.getKey(), PhotoItem.TYPE_HEADER)); // Add date header
            for (String path : entry.getValue()) {
                itemList.add(new PhotoItem(path)); // Add photo items
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PhotoItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == PhotoItem.TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            String date = itemList.get(position).getText();
            headerViewHolder.dateTextView.setText(date);
        } else {
            PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
            String photoPath = itemList.get(position).getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                photoViewHolder.imageView.setImageBitmap(bitmap);
            } else {
                // handle if the bitmap is null
                photoViewHolder.imageView.setImageResource(R.drawable.placeholder_image); // replace with a placeholder image resource if needed
            }

            photoViewHolder.imageView.setOnClickListener(v -> {
                ImageDialogFragment.newInstance(photoPath)
                        .show(fragmentManager, "image_dialog");
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
