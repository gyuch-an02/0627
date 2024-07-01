package com.example.LifeSync;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.LifeSync.contacts.ContactsFragment;
import com.example.LifeSync.photos.PhotosFragment;
import com.example.LifeSync.todos.ToDoFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class MainActivity extends AppCompatActivity {
    private static final int MULTIPLE_PERMISSIONS = 100;

    private final String[] PERMISSIONS_32 = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final String[] PERMISSIONS_33 = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    private final String[] PERMISSIONS_34 = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 권한 요청
        requestPermissions();

        ViewPager2 viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyPagerAdapter(this));

        TabLayout tabLayout = findViewById(R.id.store_fragment_tablayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("연락처");
                    break;
                case 1:
                    tab.setText("사진");
                    break;
                case 2:
                    tab.setText("Todo");
                    break;
            }
        }).attach();
    }

    private void requestPermissions() {
        String[] permissionsToRequest;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsToRequest = PERMISSIONS_34;
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest = PERMISSIONS_33;
        } else {
            permissionsToRequest = PERMISSIONS_32;
        }

        boolean permissionsNeeded = false;
        for (String permission : permissionsToRequest) {
            // 사용자가 permission 허용하지 않았다면
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded = true;
                break;
            }
        }
        if (permissionsNeeded) {
            // permission 허용 요청 다이얼로그 표시
            ActivityCompat.requestPermissions(this, permissionsToRequest, MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f0");
            if (fragment instanceof ContactsFragment) {
                ((ContactsFragment) fragment).loadContacts();
            }
        }
    }

    private static class MyPagerAdapter extends FragmentStateAdapter {
        private final String[] tabTitles = new String[]{"연락처", "사진", "Todo"};

        MyPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ContactsFragment();
                case 1:
                    return new PhotosFragment();
                case 2:
                    return new ToDoFragment();
                default:
                    throw new IllegalArgumentException("Invalid position");
            }
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
}
