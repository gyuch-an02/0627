package com.example.DailyTag.contacts;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.DailyTag.R;

public class ContactDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        ImageView profileImageView = findViewById(R.id.profileImageView);
        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView phoneTextView = findViewById(R.id.phoneTextView);

        // Retrieve the contact details from the intent
        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        String name = getIntent().getStringExtra("CONTACT_NAME");
        String phoneNumber = getIntent().getStringExtra("CONTACT_PHONE_NUMBER");
        Bitmap profileImage = getIntent().getParcelableExtra("CONTACT_IMAGE");
        Log.d("ContactDetailsActivity","phoneNumber " + phoneNumber);
        // Set the contact details to the views
        nameTextView.setText(name);
        phoneTextView.setText(phoneNumber);
        if (profileImage != null) {
            profileImageView.setImageBitmap(profileImage);
        } else {
            profileImageView.setImageResource(R.drawable.ic_default_profile); // default image
        }
    }
}
