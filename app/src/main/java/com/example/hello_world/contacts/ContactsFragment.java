package com.example.hello_world.contacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;

import com.example.hello_world.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contacts = new ArrayList<>();

    private ActivityResultLauncher<Intent> addContactActivityLauncher;
    private ActivityResultLauncher<String> requestReadContactsPermissionLauncher;
    private ActivityResultLauncher<String> requestWriteContactsPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addContactActivityLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        loadContacts();
                    }
                });

        requestReadContactsPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        loadContacts();
                    } else {
                        Log.e(TAG, "Permission not granted to read contacts");
                    }
                });

        requestWriteContactsPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        Intent intent = new Intent(getActivity(), AddContactActivity.class);
                        addContactActivityLauncher.launch(intent);
                    } else {
                        Log.e(TAG, "Permission not granted to write contacts");
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        ListView listView = view.findViewById(R.id.listView);
        SearchView searchView = view.findViewById(R.id.searchView);
        FloatingActionButton addContactButton = view.findViewById(R.id.addContactButton);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, contacts);
        listView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestReadContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        } else {
            loadContacts();
        }

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
                addContactActivityLauncher.launch(intent);
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
            contacts.clear();
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

            // 가나다순으로 정렬

            contacts.sort(new Comparator<String>() {
                @Override
                public int compare(String contact1, String contact2) {
                    int result = compareKorean(contact1, contact2);
                    System.out.println("Comparing \"" + contact1 + "\" and \"" + contact2 + "\" -> " + result);
                    return result;
                }

                private int compareKorean(String str1, String str2) {
                    String normalizedStr1 = normalizeKoreanString(str1);
                    String normalizedStr2 = normalizeKoreanString(str2);
                    System.out.println("Normalized \"" + str1 + "\" to \"" + normalizedStr1 + "\"");
                    System.out.println("Normalized \"" + str2 + "\" to \"" + normalizedStr2 + "\"");
                    return normalizedStr1.compareTo(normalizedStr2);
                }

                private String normalizeKoreanString(String str) {
                    if (str.isEmpty()) {
                        return str;
                    }

                    StringBuilder normalized = new StringBuilder();
                    boolean firstCharProcessed = false;

                    for (char ch : str.toCharArray()) {
                        if (ch >= 0x3131 && ch <= 0x314E) {  // 자음 범위: ㄱ(0x3131) ~ ㅎ(0x314E)
                            if (!firstCharProcessed) {
                                normalized.append(convertConsonantToChar(ch));
                                firstCharProcessed = true;
                            } else {
                                normalized.append(ch);
                            }
                        } else {
                            normalized.append(ch);
                            firstCharProcessed = true;
                        }
                    }

                    return normalized.toString();
                }

                private char convertConsonantToChar(char consonant) {
                    // 모든 초성 배열
                    int[] initialConsonants = {
                            0x3131, // ㄱ
                            0x3132, // ㄲ
                            0x3134, // ㄴ
                            0x3137, // ㄷ
                            0x3138, // ㄸ
                            0x3139, // ㄹ
                            0x3141, // ㅁ
                            0x3142, // ㅂ
                            0x3143, // ㅃ
                            0x3145, // ㅅ
                            0x3146, // ㅆ
                            0x3147, // ㅇ
                            0x3148, // ㅈ
                            0x3149, // ㅉ
                            0x314A, // ㅊ
                            0x314B, // ㅋ
                            0x314C, // ㅌ
                            0x314D, // ㅍ
                            0x314E  // ㅎ
                    };

                    // 자음의 인덱스를 찾는다
                    int index = -1;
                    for (int i = 0; i < initialConsonants.length; i++) {
                        if (consonant == initialConsonants[i]) {
                            index = i;
                            break;
                        }
                    }

                    if (index == -1) {
                        throw new IllegalArgumentException("Invalid consonant: " + consonant);
                    }

                    int baseCode = 0xAC00; // '가'의 유니코드 값
                    return (char) (baseCode + index * 588); // 초성 하나가 588의 간격을 가짐
                }
            });

            adapter.notifyDataSetChanged();
        }
    }
}
