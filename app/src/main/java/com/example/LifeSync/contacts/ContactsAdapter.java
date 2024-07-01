package com.example.LifeSync.contacts;

import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.LifeSync.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContactsAdapter extends BaseAdapter implements Filterable {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    private final Context context;
    private final List<Object> items;
    private List<Object> filteredItems;
    private ContactFilter contactFilter;
    private final ContactActionListener contactActionListener;

    public ContactsAdapter(Context context, List<Object> items, ContactActionListener contactActionListener) {
        this.context = context;
        this.items = items;
        this.filteredItems = items;
        this.contactActionListener = contactActionListener;
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return filteredItems.get(position) instanceof String ? TYPE_HEADER : TYPE_CONTACT;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // headers and contacts
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (viewType == TYPE_HEADER) {
                convertView = inflater.inflate(R.layout.item_contact_header, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.item_contact, parent, false);
            }
        }

        if (viewType == TYPE_HEADER) {
            TextView headerTextView = convertView.findViewById(R.id.headerTextView);
            headerTextView.setText((String) filteredItems.get(position));
        } else {
            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            TextView phoneTextView = convertView.findViewById(R.id.phoneTextView);
            Contact contact = (Contact) filteredItems.get(position);
            nameTextView.setText(contact.getName());
            phoneTextView.setText(contact.getPhoneNumber());

            convertView.setOnLongClickListener(v -> {
                showPopupMenu(v, contact);
                return true;
            });

            convertView.setOnClickListener(v -> {
                contactActionListener.onContactAction(contact);
            });
        }

        return convertView;
    }

    private void showPopupMenu(View view, Contact contact) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.contact_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_modify) {
                contactActionListener.onContactAction(contact);
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(contact);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog(Contact contact) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete the contact: " + contact.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> deleteContact(contact))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteContact(Contact contact) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.getId());
        contentResolver.delete(contactUri, null, null);
        Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show();

        // Remove contact and possibly the header
        String header = getHeaderForContact(contact);
        items.remove(contact);
        filteredItems.remove(contact);

        // Check if the header should be removed
        boolean hasMoreContacts = false;
        for (Object item : filteredItems) {
            if (item instanceof Contact && getHeaderForContact((Contact) item).equals(header)) {
                hasMoreContacts = true;
                break;
            }
        }

        if (!hasMoreContacts) {
            items.remove(header);
            filteredItems.remove(header);
        }

        notifyDataSetChanged();
    }

    private String getHeaderForContact(Contact contact) {
        char firstChar = contact.getName().charAt(0);
        if (firstChar >= 0xAC00 && firstChar <= 0xD7A3) {
            int base = firstChar - 0xAC00;
            int initialConsonantIndex = base / (21 * 28);
            char[] initialConsonants = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
            return String.valueOf(initialConsonants[initialConsonantIndex]);
        } else if (Character.isLetter(firstChar)) {
            return String.valueOf(firstChar).toUpperCase();
        } else {
            return "#";
        }
    }

    @Override
    public Filter getFilter() {
        if (contactFilter == null) {
            contactFilter = new ContactFilter();
        }
        return contactFilter;
    }

    private class ContactFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = items;
                results.count = items.size();
            } else {
                String filterString = constraint.toString().toLowerCase();
                List<Object> filteredList = new ArrayList<>();
                Map<String, List<Contact>> groupedContacts = new LinkedHashMap<>();

                for (Object item : items) {
                    if (item instanceof String) {
                        groupedContacts.put((String) item, new ArrayList<>());
                    } else {
                        Contact contact = (Contact) item;
                        String header = getHeaderForContact(contact);
                        if (!groupedContacts.containsKey(header)) {
                            groupedContacts.put(header, new ArrayList<>());
                        }
                        if (contact.getName().toLowerCase().contains(filterString) || contact.getPhoneNumber().contains(filterString)) {
                            Objects.requireNonNull(groupedContacts.get(header)).add(contact);
                        }
                    }
                }

                for (Map.Entry<String, List<Contact>> entry : groupedContacts.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        filteredList.add(entry.getKey());
                        filteredList.addAll(entry.getValue());
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<Object>) results.values;
            notifyDataSetChanged();
        }
    }

    public interface ContactActionListener {
        void onContactAction(Contact contact);
    }

    public static class Contact {
        private final long id;
        private final String name;
        private final String phoneNumber;

        public Contact(long id, String name, String phoneNumber) {
            this.id = id;
            this.name = name;
            this.phoneNumber = phoneNumber;
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
    }
}
