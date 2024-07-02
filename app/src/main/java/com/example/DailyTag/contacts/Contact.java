package com.example.DailyTag.contacts;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.InputStream;

public class Contact {
    private final long id;
    private String name;
    private String phoneNumber;
    private Bitmap profileImage;

    public Contact(long id, String name, String phoneNumber, Bitmap profileImage) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void loadContactDetails(Context context) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);

        // Query the contact details
        Cursor cursor = context.getContentResolver().query(contactUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            this.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            // Load the profile image
            this.profileImage = loadContactPhoto(context, id);

            // Load the phone number
            this.phoneNumber = getPhoneNumber(context, id);

            cursor.close();
        }
    }

    private Bitmap loadContactPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream photoInputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri);
        return photoInputStream != null ? BitmapFactory.decodeStream(photoInputStream) : null;
    }

    private String getPhoneNumber(Context context, long contactId) {
        String phoneNumber = null;
        Cursor phoneCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{String.valueOf(contactId)},
                null);

        if (phoneCursor != null && phoneCursor.moveToFirst()) {
            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneCursor.close();
        }

        return phoneNumber;
    }
}
