package com.example.DailyTag.contacts;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactManager {

    public static List<String> getContactNames(Context context) {
        List<String> contactNames = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (name != null && !name.isEmpty()) {
                    contactNames.add(name);
                }
            }
            cursor.close();
        }
        return contactNames;
    }

    @SuppressLint("Range")
    public static List<Contact> getContacts(Context context) {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);

                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Bitmap profileImage = getContactPhoto(context, id);

                            contacts.add(new Contact(Long.parseLong(id), name, phoneNumber, profileImage));
                        }
                        phoneCursor.close();
                    }
                }
            }
            cursor.close();
        }

        return contacts;
    }

    private static Bitmap getContactPhoto(Context context, String contactId) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream photoStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri);
        if (photoStream != null) {
            return BitmapFactory.decodeStream(photoStream);
        }
        return null;
    }

    public static String getInitialGroup(String name) {
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
}
