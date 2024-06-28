package com.example.hello_world.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hello_world.R;

import java.util.List;

public class ContactsAdapter extends BaseAdapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    private Context context;
    private List<Object> items;

    public ContactsAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_CONTACT;
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
            headerTextView.setText((String) items.get(position));
        } else {
            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            TextView phoneTextView = convertView.findViewById(R.id.phoneTextView);
            ContactsAdapter.Contact contact = (ContactsAdapter.Contact) items.get(position);
            nameTextView.setText(contact.getName());
            phoneTextView.setText(contact.getPhoneNumber());
        }

        return convertView;
    }

    public static class Contact {
        private String name;
        private String phoneNumber;

        public Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }
}
