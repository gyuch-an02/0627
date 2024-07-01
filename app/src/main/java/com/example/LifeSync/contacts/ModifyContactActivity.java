package com.example.LifeSync.contacts;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LifeSync.R;

import java.util.ArrayList;

public class ModifyContactActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText phoneEditText;
    private Button saveButton;
    private long contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        nameEditText = findViewById(R.id.editTextName);
        phoneEditText = findViewById(R.id.editTextPhone);
        saveButton = findViewById(R.id.saveButton);

        contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            loadContact(contactId);
        }

        saveButton.setOnClickListener(v -> saveContact());
    }

    private void loadContact(long contactId) {
        // Query the contact and pre-fill the fields
        ContentResolver contentResolver = getContentResolver();
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Cursor cursor = contentResolver.query(contactUri, null, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    nameEditText.setText(name);

                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);

                    if (phoneCursor != null) {
                        if (phoneCursor.moveToFirst()) {
                            @SuppressLint("Range") String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phoneEditText.setText(phoneNumber);
                        }
                        phoneCursor.close();
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void saveContact() {
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        if (!name.isEmpty() && !phone.isEmpty()) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            // Update display name
            String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?";
            String[] nameParams = new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, nameParams)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());

            // Update phone number
            String[] phoneParams = new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, phoneParams)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .build());

            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Indicate success
                finish();
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error updating contact", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
