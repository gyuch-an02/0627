package com.example.hello_world;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contacts = new ArrayList<>();

    private final ActivityResultLauncher<String> requestReadContactsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadContacts();
                } else {
                    Log.e(TAG, "Permission not granted to read contacts");
                }
            });

    private final ActivityResultLauncher<String> requestWriteContactsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Intent intent = new Intent(getActivity(), AddContactActivity.class);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Permission not granted to write contacts");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        ListView listView = view.findViewById(R.id.listView);
        SearchView searchView = view.findViewById(R.id.searchView);
        Button addContactButton = view.findViewById(R.id.addContactButton);

        ArrayList<String> contacts = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

            Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    if (nameIndex != -1 && numberIndex != -1) {
                        String name = cursor.getString(nameIndex);
                        String phoneNumber = cursor.getString(numberIndex);
                        contacts.add(name + " : " + phoneNumber);
                    } else {
                        Log.e(TAG, "Column index is -1"); // TAG 변수 사용
                    }
                }
                cursor.close();
            }
        }


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestReadContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        } else {
            loadContacts();
        }

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, contacts);
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        addContactButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestWriteContactsPermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS);
            } else {
                Intent intent = new Intent(getActivity(), AddContactActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadContacts() {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (nameIndex != -1 && numberIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    String phoneNumber = cursor.getString(numberIndex);
                    contacts.add(name + " : " + phoneNumber);
                } else {
                    Log.e(TAG, "Column index is -1");
                }
            }
            cursor.close();
        }
    }
}
