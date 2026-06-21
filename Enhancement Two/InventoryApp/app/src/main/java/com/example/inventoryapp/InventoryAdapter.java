/*
 * File: InventoryAdapter.java
 * Description: Bridges the raw database data and the visual list user interface (RecyclerView).
 * Author: Aaditya Fadnavis
 * Date: May 21, 2026
 */

package com.example.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private static final int LOW_INVENTORY_ALERT_TRIGGER = 3;
    private final InventoryActivity activity;
    private final DatabaseHelper db;
    private final Runnable refreshCallback;
    private Cursor cursor;

    // Column indices cached to avoid expensive lookups during scrolling.
    private int idColumnIndex = -1;
    private int nameColumnIndex = -1;
    private int quantityColumnIndex = -1;

    public InventoryAdapter(InventoryActivity activity, Cursor cursor, DatabaseHelper db, Runnable refreshCallback) {
        this.activity = activity;
        this.db = db;
        this.refreshCallback = refreshCallback;
        swapCursor(cursor); // Initialize and cache indices cleanly.
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.inventory_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Ensure cursor is valid and data is accessible.
        if (cursor == null || !cursor.moveToPosition(position)) {
            return;
        }

        // Optimized pre-cached column indices.
        final int id = cursor.getInt(idColumnIndex);
        final String name = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        holder.itemName.setText(name);

        Context context = holder.itemView.getContext();
        String formattedText = context.getString(R.string.item_quantity_format, quantity);
        holder.itemQuantity.setText(formattedText);

        // Click listeners.
        holder.deleteButton.setOnClickListener(v -> {
            db.deleteItem(id);
            refreshCallback.run();
        });

        holder.btnPlus.setOnClickListener(v -> {
            db.updateItemQuantity(id, quantity + 1);
            refreshCallback.run();
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (quantity > 0) {
                int newQty = quantity - 1;
                db.updateItemQuantity(id, newQty);

                if (newQty < LOW_INVENTORY_ALERT_TRIGGER) {
                    activity.sendLowInventorySMS(name, newQty);
                }
                refreshCallback.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    // Old cursor swapped out to ensure complete closure and stop memory leaks.
    public void swapCursor(Cursor newCursor) {
        if (this.cursor == newCursor) {
            return;
        }

        // Close old cursor to free native memory allocation.
        if (this.cursor != null) {
            this.cursor.close();
        }

        this.cursor = newCursor;

        if (newCursor != null) {
            // Cache column lookups once per dataset swap, rather than on every scroll layout row.
            this.idColumnIndex = newCursor.getColumnIndexOrThrow("id");
            this.nameColumnIndex = newCursor.getColumnIndexOrThrow("name");
            this.quantityColumnIndex = newCursor.getColumnIndexOrThrow("quantity");
            notifyDataSetChanged();
        } else {
            this.idColumnIndex = -1;
            this.nameColumnIndex = -1;
            this.quantityColumnIndex = -1;
            notifyDataSetChanged();
        }
    }

    // Optimization cache for the user interface layout elements for an item row.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView itemName;
        final TextView itemQuantity;
        final Button deleteButton;
        final Button btnPlus;
        final Button btnMinus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            deleteButton = itemView.findViewById(R.id.delete_button);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
        }
    }
}