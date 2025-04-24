package com.lab.safe_alert_esanu_cristian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavouriteContacts extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    public static ArrayList<Contact> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        recyclerView = findViewById(R.id.contactsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //contactList.add(new Contact("Chris Wilson", "+40777777777"));


        contactAdapter = new ContactAdapter(contactList, this);
        recyclerView.setAdapter(contactAdapter);

        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void addContact(View view) {
        EditText nameInput = findViewById(R.id.nameInput);
        EditText phoneInput = findViewById(R.id.phoneInput);

        String name = nameInput.getText().toString();
        String phone = phoneInput.getText().toString();

        if (!name.isEmpty() && !phone.isEmpty()) {
            contactList.add(new Contact(name, phone));
            contactAdapter.notifyDataSetChanged();
            nameInput.setText("");
            phoneInput.setText("");
            Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
        }
    }
}
