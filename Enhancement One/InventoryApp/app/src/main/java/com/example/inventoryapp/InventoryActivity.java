/*
 * File: InventoryActivity.java
 * Description: Handles logic for the inventory page (items list, sms message, add new item).
 * Author: Aaditya Fadnavis
 * Date: May 21, 2026
 */

package com.example.inventoryapp;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class InventoryActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_SMS = 100;
    private static final String TAG_SMS = "SMS_LOG";
    private static final int INVALID_USER_ID = -1;
    private static final int LOW_INVENTORY_THRESHOLD = 2;

    private DatabaseHelper db;
    private RecyclerView recyclerView;
    private SwitchCompat smsSwitch;
    private int currentUserId;
    private InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_activity);

        currentUserId = getIntent().getIntExtra(LoginActivity.EXTRA_USER_ID, INVALID_USER_ID);
        if (currentUserId == INVALID_USER_ID) {
            Toast.makeText(this, "Error: Invalid User Session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.inventory_recycler_view);
        smsSwitch = findViewById(R.id.sms_permission_switch);
        FloatingActionButton fab = findViewById(R.id.add_item_fab);

        loadData();

        smsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkSmsPermission();
            }
        });

        fab.setOnClickListener(v -> showAddDialog());
    }

    // Load/Refresh the cursor data and safely swap it out to avoid memory leaks.
    public void loadData() {
        Cursor cursor = db.getItemsByUser(currentUserId);

        if (adapter == null) {
            adapter = new InventoryAdapter(this, cursor, db, this::loadData);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.swapCursor(cursor);
        }
    }

    // Display dialog to add items with thorough defensive input validation.
    private void showAddDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_item);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText nameInput = dialog.findViewById(R.id.edit_item_name);
        EditText qtyInput = dialog.findViewById(R.id.edit_item_quantity);
        Button saveBtn = dialog.findViewById(R.id.btn_save_item);

        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String qtyStr = qtyInput.getText().toString().trim();

            // Check for empty values
            if (name.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prevent NumberFormatException crashes
            try {
                int quantity = Integer.parseInt(qtyStr);
                if (quantity < 0) {
                    Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.addItem(name, quantity, currentUserId);
                loadData();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity number entered", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    // Low Inventory message logic.
    public void sendLowInventorySMS(String itemName, int quantity) {
        if (quantity > LOW_INVENTORY_THRESHOLD) {
            return;
        }

        if (smsSwitch.isChecked()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                String logMsg = "SMS SENT to XXX-XXXX: ALERT! " + itemName + " is low. Only " + quantity + " left in stock.";
                Log.d(TAG_SMS, logMsg);
                Toast.makeText(this, "Low inventory alert logged!", Toast.LENGTH_SHORT).show();
            } else {
                checkSmsPermission();
            }
        }
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
                smsSwitch.setChecked(false);
            }
        }
    }
}