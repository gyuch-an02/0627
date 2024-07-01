package com.example.LifeSync.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;

import com.example.LifeSync.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ContactsFragment extends Fragment {

    private ContactsAdapter adapter;
    private static final ArrayList<Object> contactsAndHeaders = new ArrayList<>();

    private ActivityResultLauncher<Intent> contactActivityLauncher;
    private ActivityResultLauncher<String> requestReadContactsPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactActivityLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        loadContacts();
                    }
                });

        requestReadContactsPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        loadContacts();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        SearchView searchView = view.findViewById(R.id.searchView);
        ListView listView = view.findViewById(R.id.listView);
        FloatingActionButton addContactButton = view.findViewById(R.id.addContactButton);

        adapter = new ContactsAdapter(getContext(), contactsAndHeaders, contact -> {
            Intent intent = new Intent(getActivity(), ContactActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            contactActivityLauncher.launch(intent);
        });
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        addContactButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ContactActivity.class);
            contactActivityLauncher.launch(intent);
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            requestReadContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }

        return view;
    }

    @SuppressLint("Range")
    public void loadContacts() {
        contactsAndHeaders.clear();
        Cursor cursor = requireActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        Map<String, List<ContactsAdapter.Contact>> groupedContacts = new TreeMap<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Bitmap profileImage = loadContactPhoto(id);
                String initial = getInitialGroup(name);

                if (!groupedContacts.containsKey(initial)) {
                    groupedContacts.put(initial, new ArrayList<>());
                }
                Objects.requireNonNull(groupedContacts.get(initial)).add(new ContactsAdapter.Contact(id, name, phoneNumber, profileImage));
            }
            cursor.close();
        }

        for (Map.Entry<String, List<ContactsAdapter.Contact>> entry : groupedContacts.entrySet()) {
            // Add header and contacts to the list
            contactsAndHeaders.add(entry.getKey()); // Add header
            contactsAndHeaders.addAll(entry.getValue()); // Add contacts
        }
        adapter.notifyDataSetChanged();
    }

    private Bitmap loadContactPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream photoInputStream = ContactsContract.Contacts.openContactPhotoInputStream(requireActivity().getContentResolver(), contactUri);
        return photoInputStream != null ? BitmapFactory.decodeStream(photoInputStream) : null;
    }

    private String getInitialGroup(String name) {
        char firstChar = name.charAt(0);
        if (firstChar >= 0xAC00 && firstChar <= 0xD7A3) {
            // Hangul character
            int base = firstChar - 0xAC00;
            int initialConsonantIndex = base / (21 * 28);
            char[] initialConsonants = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
            return String.valueOf(initialConsonants[initialConsonantIndex]);
        } else if (Character.isLetter(firstChar)) {
            // Alphabet character
            return String.valueOf(firstChar).toUpperCase();
        } else {
            // Other characters
            return "#";
        }
    }

    public static List<String> getContactNames() {
        List<String> contactNames = new ArrayList<>();
        for (Object item : contactsAndHeaders) {
            if (item instanceof ContactsAdapter.Contact) {
                contactNames.add(((ContactsAdapter.Contact) item).getName());
            }
        }
        return contactNames;
    }
}
