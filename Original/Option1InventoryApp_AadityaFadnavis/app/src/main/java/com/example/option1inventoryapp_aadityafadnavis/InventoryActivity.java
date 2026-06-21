package com.example.option1inventoryapp_aadityafadnavis;

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
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InventoryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private Switch smsSwitch;
    private int currentUserId;

    // Set up the UI, retrieve user information, and prepare interaction logic.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.inventory_recycler_view);
        smsSwitch = findViewById(R.id.sms_permission_switch);
        FloatingActionButton fab = findViewById(R.id.add_item_fab);

        loadData();

        smsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkSmsPermission();
        });

        fab.setOnClickListener(v -> showAddDialog());
    }

    // Load data.
    public void loadData() {
        Cursor cursor = db.getItemsByUser(currentUserId);
        adapter = new InventoryAdapter(this, cursor, db, this::loadData);
        recyclerView.setAdapter(adapter);
    }

    // Add new item pop up window.
    private void showAddDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_item);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText nameInput = dialog.findViewById(R.id.edit_item_name);
        EditText qtyInput = dialog.findViewById(R.id.edit_item_quantity);
        Button saveBtn = dialog.findViewById(R.id.btn_save_item);

        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String qtyStr = qtyInput.getText().toString().trim();

            if (!name.isEmpty() && !qtyStr.isEmpty()) {
                db.addItem(name, Integer.parseInt(qtyStr), currentUserId);
                loadData();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // Low Inventory messenger (when items >= 2).
    public void sendLowInventorySMS(String itemName, int quantity) {
        if (smsSwitch != null && smsSwitch.isChecked()) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                String logMsg = "SMS SENT to XXX-XXXX: ALERT! " + itemName + " is low. Only " + quantity + " left in stock.";

                Log.d("SMS_LOG", logMsg);

                Toast.makeText(this, "Low inventory alert logged!", Toast.LENGTH_SHORT).show();

            } else {
                checkSmsPermission();
            }
        }
    }

    // Checks for sms permission.
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 100);
        }
    }
}