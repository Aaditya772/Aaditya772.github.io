/*
* File: DatabaseHelper.java
* Description: Manages creation, upgrades, and basic CRUD (create, read, update, delete) operations
* for a relational schema of users and their associated databases and inventory items.
* Author: Aaditya Fadnavis
* Date: June 6, 2026
*/

package com.example.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";

    // Upgraded database version from 2 to 3.
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_DATABASES = "databases";
    private static final String COL_DATABASE_ID = "database_id";
    private static final String COL_DATABASE_NAME = "database_name";
    private static final String COL_DATABASE_DESCRIPTION = "database_description";

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

    // Search inventory items by name for a specific user.
    public Cursor searchItemsByUser(int userId, String searchText) {

        if (userId <= 0) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS +
                        " WHERE " + COL_USER_ID_FK + " = ?" +
                        " AND " + COL_ITEM_NAME + " LIKE ?",
                new String[]{
                        String.valueOf(userId),
                        "%" + searchText + "%"
                }
        );
    }

    // Search inventory items by name for a specific database only.
    public Cursor searchItemsByDatabase(
            int databaseId,
            String searchText) {

        if (databaseId <= 0) {
            return null;
        }

        SQLiteDatabase db =
                getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " +
                        TABLE_ITEMS +
                        " WHERE " +
                        COL_DATABASE_ID +
                        "=? AND " +
                        COL_ITEM_NAME +
                        " LIKE ?",
                new String[]{
                        String.valueOf(databaseId),
                        "%" + searchText + "%"
                });
    }

    // Create required tables for storing user, item, and database attributes.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Validation check to prevent null database executions.
        if (db == null) return;

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_DATABASES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_ID_FK + " INTEGER, " +
                COL_DATABASE_NAME + " TEXT, " +
                COL_DATABASE_DESCRIPTION + " TEXT, " +
                "FOREIGN KEY(" + COL_USER_ID_FK + ") REFERENCES " +
                TABLE_USERS + "(" + COL_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATABASE_ID + " INTEGER, " +
                COL_ITEM_NAME + " TEXT, " +
                COL_QUANTITY + " INTEGER, " +
                "FOREIGN KEY(" + COL_DATABASE_ID + ") REFERENCES " +
                TABLE_DATABASES + "(" + COL_ID + ") ON DELETE CASCADE)");
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

    // Add new item and associate it with specific valid identifiers such as the database.
    public void addItem(String name, int quantity, int databaseId) {
        // Error checking on input bounds for security.
        if (name == null || name.trim().isEmpty() || quantity < 0 || databaseId <= 0) {
            throw new IllegalArgumentException("Cannot insert item: Invalid structural details or user boundaries provided.");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_DATABASE_ID, databaseId);
        db.insert(TABLE_ITEMS, null, values);
    }

    // Method to retrieve all specific items that belong only to the specific database.
    public Cursor getItemsByDatabase(
            int databaseId) {
        if (databaseId <= 0) {
            return null;
        }

        SQLiteDatabase db =
                getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " +
                        TABLE_ITEMS +
                        " WHERE " +
                        COL_DATABASE_ID +
                        "=?",
                new String[]{
                        String.valueOf(databaseId)
                });
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

    // Add database method.
    public void addDatabase(String name,
                            String description,
                            int userId) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_DATABASE_NAME, name);
        values.put(COL_DATABASE_DESCRIPTION, description);
        values.put(COL_USER_ID_FK, userId);

        db.insert(TABLE_DATABASES,
                null,
                values);
    }

    // Retrieve database method to retrieve all databases of a specific user.
    public Cursor getDatabasesByUser(int userId) {

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " +
                        TABLE_DATABASES +
                        " WHERE " +
                        COL_USER_ID_FK +
                        "=?",
                new String[]{
                        String.valueOf(userId)
                });
    }

    // Update database description method.
    public void updateDatabaseDescription(
            int databaseId,
            String description) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                COL_DATABASE_DESCRIPTION,
                description
        );

        db.update(
                TABLE_DATABASES,
                values,
                COL_ID + "=?",
                new String[]{
                        String.valueOf(databaseId)
                });
    }

    // Delete database method.
    public void deleteDatabase(
            int databaseId) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.delete(
                TABLE_DATABASES,
                COL_ID + "=?",
                new String[]{
                        String.valueOf(databaseId)
                });
    }
}