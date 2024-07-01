package com.example.DailyTag.contacts;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.DailyTag.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {

    private static final String TAG = "ContactActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    private EditText nameEditText;
    private EditText phoneEditText;
    private Button saveButton;
    private ImageView profileImageView;
    private Bitmap profileImageBitmap;
    private Uri imageUri;
    private long contactId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        nameEditText = findViewById(R.id.editTextName);
        phoneEditText = findViewById(R.id.editTextPhone);
        saveButton = findViewById(R.id.saveButton);
        profileImageView = findViewById(R.id.profile_image);

        // Set default profile image as rounded
        Bitmap defaultProfileImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_profile);
        profileImageView.setImageBitmap(getRoundedBitmap(defaultProfileImage));

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

        profileImageView.setOnClickListener(v -> openImagePicker());

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            if (!name.isEmpty() && !phone.isEmpty()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_WRITE_CONTACTS);
                } else {
                    if (isEditMode) {
                        updateContact(name, phone);
                    } else {
                        addContact(name, phone);
                    }
                }
            } else {
                Toast.makeText(ContactActivity.this, "이름과 전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            isEditMode = true;
            loadContact(contactId);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageBitmap = resizeBitmap(profileImageBitmap, 200, 200); // Resize to reduce the size
                profileImageView.setImageBitmap(getRoundedBitmap(profileImageBitmap));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error loading image: " + e.getMessage());
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap original, int width, int height) {
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    private Bitmap getRoundedBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int radius = Math.min(width, height) / 2;

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        float roundPx = radius;

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xFF000000);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_WRITE_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String name = nameEditText.getText().toString();
                String phone = phoneEditText.getText().toString();
                if (isEditMode) {
                    updateContact(name, phone);
                } else {
                    addContact(name, phone);
                }
            } else {
                Toast.makeText(this, "Contacts write permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "External storage read permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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

                    // Load profile image if available
                    Cursor photoCursor = contentResolver.query(
                            ContactsContract.Data.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                            ContactsContract.Data.CONTACT_ID + " = ? AND " +
                                    ContactsContract.Data.MIMETYPE + " = ?",
                            new String[]{id, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE},
                            null);

                    if (photoCursor != null) {
                        if (photoCursor.moveToFirst()) {
                            byte[] photoBytes = photoCursor.getBlob(0);
                            if (photoBytes != null) {
                                profileImageBitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                                profileImageView.setImageBitmap(getRoundedBitmap(profileImageBitmap));
                            }
                        }
                        photoCursor.close();
                    }
                }
            } finally {
                cursor.close();
            }
        }
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

        // Add profile image if available
        if (profileImageBitmap != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); // Compress further to reduce size
                byte[] imageBytes = stream.toByteArray();

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                        .build());
            } catch (Exception e) {
                Log.e(TAG, "Error adding profile image: " + e.getMessage());
            }
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Log.d(TAG, "Contact added: " + displayName + " : " + phoneNumber);
            Toast.makeText(this, "연락처가 추가되었습니다", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Indicate success
            finish(); // Finish the activity
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error adding contact: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateContact(String displayName, String phoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Update display name
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";
        String[] nameParams = new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, nameParams)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());

        // Update phone number
        String[] phoneParams = new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, phoneParams)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .build());

        // Check if the profile image exists
        boolean hasProfileImage = false;
        ContentResolver contentResolver = getContentResolver();
        Cursor photoCursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                ContactsContract.Data.CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE},
                null);

        if (photoCursor != null) {
            hasProfileImage = photoCursor.moveToFirst() && photoCursor.getBlob(0) != null;
            photoCursor.close();
        }

        // Update or insert profile image if a new one is set
        if (profileImageBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); // Compress to reduce size
            byte[] imageBytes = stream.toByteArray();

            if (hasProfileImage) {
                // Update existing profile image
                String photoWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ?";
                String[] photoParams = new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(photoWhere, photoParams)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                        .build());
            } else {
                // Insert new profile image
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                        .build());
            }
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Indicate success
            finish();
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating contact", Toast.LENGTH_SHORT).show();
        }
    }


}
