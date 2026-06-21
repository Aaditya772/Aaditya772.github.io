package com.example.option1inventoryapp_aadityafadnavis;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private InventoryActivity activity;
    private Cursor cursor;
    private DatabaseHelper db;
    private Runnable refreshCallback;

    // Bring necessary information from InventoryActivity.java to InventoryAdapter.java.
    public InventoryAdapter(InventoryActivity activity, Cursor cursor, DatabaseHelper db, Runnable refreshCallback) {
        this.activity = activity;
        this.cursor = cursor;
        this.db = db;
        this.refreshCallback = refreshCallback;
    }

    // Objectify inventory_item.xml so it can be displayed.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.inventory_item, parent, false);
        return new ViewHolder(view);
    }

    // Fill the new object created previously with real data.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));

        holder.itemName.setText(name);
        holder.itemQuantity.setText("Qty: " + quantity);

        // Set delete button.
        holder.deleteButton.setOnClickListener(v -> {
            db.deleteItem(id);
            refreshCallback.run();
        });

        // Set (+) button.
        holder.btnPlus.setOnClickListener(v -> {
            db.updateItemQuantity(id, quantity + 1);
            refreshCallback.run();
        });

        // Set (-) button.
        holder.btnMinus.setOnClickListener(v -> {
            if (quantity > 0) {
                int newQty = quantity - 1;
                db.updateItemQuantity(id, newQty);

                if (newQty < 3) {
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity;
        Button deleteButton, btnPlus, btnMinus;

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