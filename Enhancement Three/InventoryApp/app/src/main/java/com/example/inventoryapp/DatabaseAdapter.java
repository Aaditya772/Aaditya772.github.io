/*
 * File: DatabaseAdapter.java
 * Description: Read database records from the SQLite cursor and display them in a list where each
 * item can be opened, edited, or deleted through click actions and a popup dialog.
 * Author: Aaditya Fadnavis
 * Date: June 6, 2026
 */

package com.example.inventoryapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class DatabaseAdapter
        extends RecyclerView.Adapter<DatabaseAdapter.ViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final DatabaseHelper db;
    private final int userId;
    private final Runnable refreshCallback;
    private int idColumn = -1;
    private int nameColumn = -1;
    private int descriptionColumn = -1;

    // Initialize the adapter (store the context, database cursor, helper, user ID, and refresh
    // callback), then pre-cache the database column indexes for faster access.
    public DatabaseAdapter(
            Context context,
            Cursor cursor,
            DatabaseHelper db,
            int userId,
            Runnable refreshCallback) {

        this.context = context;
        this.cursor = cursor;
        this.db = db;
        this.userId = userId;
        this.refreshCallback = refreshCallback;

        cacheColumns();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.database_item,
                                parent,
                                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        if (cursor == null ||
                !cursor.moveToPosition(position)) {
            return;
        }

        int databaseId =
                cursor.getInt(idColumn);

        String databaseName =
                cursor.getString(nameColumn);

        String description =
                cursor.getString(descriptionColumn);

        holder.databaseName.setText(
                databaseName);

        holder.itemView.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            context,
                            InventoryActivity.class);

            intent.putExtra(
                    DatabaseListActivity.EXTRA_DATABASE_ID,
                    databaseId);

            intent.putExtra(
                    "DATABASE_NAME",
                    databaseName);

            intent.putExtra(
                    LoginActivity.EXTRA_USER_ID,
                    userId);

            context.startActivity(intent);
        });

        holder.menuButton.setOnClickListener(
                v -> showDatabaseDialog(
                        databaseId,
                        description));
    }

    private void showDatabaseDialog(
            int databaseId,
            String description) {

        Dialog dialog =
                new Dialog(context);

        dialog.setContentView(
                R.layout.database_description);

        Objects.requireNonNull(
                        dialog.getWindow())
                .setBackgroundDrawable(
                        new ColorDrawable(
                                Color.TRANSPARENT));

        EditText descriptionInput =
                dialog.findViewById(
                        R.id.database_description_input);

        Button saveButton =
                dialog.findViewById(
                        R.id.btn_save_description);

        Button deleteButton =
                dialog.findViewById(
                        R.id.btn_delete_database);

        descriptionInput.setText(
                description);

        saveButton.setOnClickListener(v -> {

            db.updateDatabaseDescription(
                    databaseId,
                    descriptionInput
                            .getText()
                            .toString()
                            .trim());

            refreshCallback.run();

            dialog.dismiss();
        });

        deleteButton.setOnClickListener(v -> {

            db.deleteDatabase(
                    databaseId);

            refreshCallback.run();

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {

        if (cursor == null) {
            return 0;
        }

        return cursor.getCount();
    }

    public void swapCursor(
            Cursor newCursor) {

        if (cursor != null) {
            cursor.close();
        }

        cursor = newCursor;

        cacheColumns();

        notifyDataSetChanged();
    }

    private void cacheColumns() {

        if (cursor == null) {
            return;
        }

        idColumn =
                cursor.getColumnIndexOrThrow(
                        "id");

        nameColumn =
                cursor.getColumnIndexOrThrow(
                        "database_name");

        descriptionColumn =
                cursor.getColumnIndexOrThrow(
                        "database_description");
    }

    // Store and link the UI elements for a single RecyclerView row (database name text and menu button)
    // for better reusability.
    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView databaseName;
        ImageButton menuButton;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            databaseName =
                    itemView.findViewById(
                            R.id.database_name);

            menuButton =
                    itemView.findViewById(
                            R.id.database_menu);
        }
    }
}