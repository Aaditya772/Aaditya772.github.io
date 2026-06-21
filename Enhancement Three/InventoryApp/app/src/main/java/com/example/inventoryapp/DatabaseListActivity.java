/*
 * File: DatabaseListActivity.java
 * Description: Handle logic for the database home page (database list, add new database).
 * Author: Aaditya Fadnavis
 * Date: June 6, 2026
 */

package com.example.inventoryapp;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class DatabaseListActivity extends AppCompatActivity {

    public static final String EXTRA_DATABASE_ID =
            "DATABASE_ID";
    private int currentUserId;
    private RecyclerView recyclerView;
    private DatabaseHelper db;
    private DatabaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database_activity);

        currentUserId =
                getIntent().getIntExtra(
                        LoginActivity.EXTRA_USER_ID,
                        -1);

        db = new DatabaseHelper(this);

        recyclerView =
                findViewById(
                        R.id.database_recycler_view);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        FloatingActionButton fab =
                findViewById(
                        R.id.add_database_fab);

        loadDatabases();

        fab.setOnClickListener(
                v -> showAddDatabaseDialog());
    }

    private void loadDatabases() {

        Cursor cursor =
                db.getDatabasesByUser(
                        currentUserId);

        if (adapter == null) {
            adapter =
                    new DatabaseAdapter(
                            this,
                            cursor,
                            db,
                            currentUserId,
                            this::loadDatabases);
            recyclerView.setAdapter(adapter);

        } else {
            adapter.swapCursor(cursor);
        }
    }

    private void showAddDatabaseDialog() {

        Dialog dialog =
                new Dialog(this);

        dialog.setContentView(
                R.layout.add_database);

        Objects.requireNonNull(
                        dialog.getWindow())
                .setBackgroundDrawable(
                        new ColorDrawable(
                                Color.TRANSPARENT));

        EditText nameInput =
                dialog.findViewById(
                        R.id.edit_database_name);

        EditText descriptionInput =
                dialog.findViewById(
                        R.id.edit_database_description);

        Button saveButton =
                dialog.findViewById(
                        R.id.btn_save_database);

        saveButton.setOnClickListener(v -> {

            String name =
                    nameInput.getText()
                            .toString()
                            .trim();

            String description =
                    descriptionInput.getText()
                            .toString()
                            .trim();

            if (name.isEmpty()) {
                return;
            }

            db.addDatabase(
                    name,
                    description,
                    currentUserId);

            loadDatabases();

            dialog.dismiss();
        });

        dialog.show();
    }
}