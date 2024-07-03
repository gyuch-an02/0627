package com.example.DailyTag.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.DailyTag.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ContactsFragment extends Fragment {

    private static final ArrayList<Object> contactsAndHeaders = new ArrayList<>();
    private ContactsAdapter adapter;
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
        List<Contact> contacts = ContactManager.getContacts(requireContext());
        Map<String, List<Contact>> groupedContacts = new TreeMap<>();

        for (Contact contact : contacts) {
            String initial = ContactManager.getInitialGroup(contact.getName());
            if (!groupedContacts.containsKey(initial)) {
                groupedContacts.put(initial, new ArrayList<>());
            }
            Objects.requireNonNull(groupedContacts.get(initial)).add(contact);
        }

        for (Map.Entry<String, List<Contact>> entry : groupedContacts.entrySet()) {
            // Add header and contacts to the list
            contactsAndHeaders.add(entry.getKey()); // Add header
            contactsAndHeaders.addAll(entry.getValue()); // Add contacts
        }

        adapter.notifyDataSetChanged();
    }
}
