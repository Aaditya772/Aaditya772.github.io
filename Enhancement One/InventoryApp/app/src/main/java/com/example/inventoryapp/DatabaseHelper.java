/*
* File: DatabaseHelper.java
* Description: Manages creation, upgrades, and basic CRUD (create, read, update, delete) operations
* for a relational schema of users and their associated inventory items.
* Author: Aaditya Fadnavis
* Date: May 21, 2026
*/

package com.example.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_ITEMS = "items";

    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    private static final String COL_ITEM_NAME = "name";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_USER_ID_FK = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create required tables for storing user and item attributes.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Validation check to prevent null database executions.
        if (db == null) return;

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_ID_FK + " INTEGER, " +
                COL_ITEM_NAME + " TEXT, " +
                COL_QUANTITY + " INTEGER, " +
                "FOREIGN KEY(" + COL_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + ") ON DELETE CASCADE)");
    }

    // Non-destructive upgrade routine to safely preserve production user data.
    // Old method (DROP TABLE) completely discarded user data.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db == null) return;

        if (oldVersion < 2) {
            // In the future, a temporary table migration method can be implemented.
            // In this project, upgrades will not be present.
        }
    }

    // Securely register a unique user. Returns false on naming conflicts or invalid inputs.
    public boolean addUser(String username, String password) {
        // Defensive parameter verification
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username.trim());
        values.put(COL_PASSWORD, password);

        return db.insert(TABLE_USERS, null, values) != -1;
    }

    // Validate if a requested username matches an existing account profile.
    public boolean checkUsernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        // Try-with-resources statement to ensure proper cursor closure state regardless of execution failures.
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username.trim()})) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    // Retrieve unique user identification. Returns -1 if no matching credential block is located.
    public int getUserId(String username, String password) {
        if (username == null || password == null) {
            return -1;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;

        try (Cursor cursor = db.rawQuery("SELECT " + COL_ID + " FROM " + TABLE_USERS + " WHERE " +
                COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?", new String[]{username.trim(), password})) {

            if (cursor != null && cursor.moveToFirst()) {
                // Resolve columns dynamically by name mapping to avoid record boundary errors.
                int idIndex = cursor.getColumnIndexOrThrow(COL_ID);
                userId = cursor.getInt(idIndex);
            }
        }
        return userId;
    }

    // Append inventory elements associated with a valid identifier.
    public void addItem(String name, int quantity, int userId) {
        // Error checking on input bounds for security.
        if (name == null || name.trim().isEmpty() || quantity < 0 || userId <= 0) {
            throw new IllegalArgumentException("Cannot insert item: Invalid structural details or user boundaries provided.");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_USER_ID_FK, userId);
        db.insert(TABLE_ITEMS, null, values);
    }

    // Data leak filter to retrieve inventory bounds tied strictly to a designated user identity.
    public Cursor getItemsByUser(int userId) {
        if (userId <= 0) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE " + COL_USER_ID_FK + " = ?", new String[]{String.valueOf(userId)});
    }

    // Adjust specific count settings assigned to a precise table index.
    public void updateItemQuantity(int id, int newQuantity) {
        if (id <= 0 || newQuantity < 0) {
            throw new IllegalArgumentException("Cannot update quantity: Structural identity constraints breached.");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_QUANTITY, newQuantity);
        db.update(TABLE_ITEMS, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Delete localized inventory item entry.
    public void deleteItem(int id) {
        if (id <= 0) return;

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }
}