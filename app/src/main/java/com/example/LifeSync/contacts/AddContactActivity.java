package com.example.LifeSync.contacts;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LifeSync.R;

import java.util.ArrayList;

public class AddContactActivity extends AppCompatActivity {

    private static final String TAG = "AddContactActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        EditText nameEditText = findViewById(R.id.editTextName);
        EditText phoneEditText = findViewById(R.id.editTextPhone);
        Button saveButton = findViewById(R.id.saveButton);

        phoneEditText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private final StringBuilder builder = new StringBuilder();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) {
                    return;
                }

                isFormatting = true;
                builder.setLength(0);

                // Remove all non-digit characters
                String digits = s.toString().replaceAll("\\D", "");
                int length = digits.length();

                // Format according to the number of digits
                for (int i = 0; i < length; i++) {
                    builder.append(digits.charAt(i));
                    if (i == 2 || i == 6) {
                        builder.append('-');
                    }
                }

                // Update the text with the new format
                phoneEditText.setText(builder.toString());
                phoneEditText.setSelection(builder.length());

                isFormatting = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            if (!name.isEmpty() && !phone.isEmpty()) {
                addContact(name, phone);
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(AddContactActivity.this, "이름과 전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addContact(String displayName, String phoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Log.d(TAG, "Contact added: " + displayName + " : " + phoneNumber);
            Toast.makeText(this, "연락처가 추가되었습니다", Toast.LENGTH_SHORT).show();
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error adding contact", e);
            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
        }
    }
}
