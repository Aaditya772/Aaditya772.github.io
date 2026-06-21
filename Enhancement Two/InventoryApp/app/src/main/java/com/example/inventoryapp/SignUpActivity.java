/*
 * File: SignUpActivity.java
 * Description: Handles logic for the sign up page.
 * Author: Aaditya Fadnavis
 * Date: May 22, 2026
 */

package com.example.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SignUpActivity extends AppCompatActivity {

    // User facing message constants.
    private static final String MSG_EMPTY_FIELDS = "Please fill in all fields";
    private static final String MSG_PASSWORD_MISMATCH = "Passwords do not match";
    private static final String MSG_USERNAME_TAKEN = "Username already exists";
    private static final String MSG_ACCOUNT_CREATED = "New Account Created!";
    private static final String MSG_REGISTRATION_FAILED = "Error creating account. Please try again.";

    // Sentinel value for invalid database IDs.
    private static final int INVALID_USER_ID = -1;

    private DatabaseHelper db;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        // Hide Action Bar to match Login screen style.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configure system windows for dark status bar icons for visibility on the light background.
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        // Initialize dependencies and views.
        db = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.signup_username);
        passwordInput = findViewById(R.id.signup_password);
        confirmPasswordInput = findViewById(R.id.signup_confirm_password);
        Button createAccountButton = findViewById(R.id.create_account_button);

        createAccountButton.setOnClickListener(v -> handleSignUp());
    }

    // Extract inputs, perform structural validation, match passwords, and check system state
    // before executing creation queries.
    private void handleSignUp() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Check for empty fields.
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast(MSG_EMPTY_FIELDS);
            return;
        }

        // Verify passwords match.
        if (!password.equals(confirmPassword)) {
            showToast(MSG_PASSWORD_MISMATCH);
            return;
        }

        // Ensure uniqueness.
        if (db.checkUsernameExists(username)) {
            showToast(MSG_USERNAME_TAKEN);
            return;
        }

        // Create the user database record.
        boolean isUserAdded = db.addUser(username, password);
        if (!isUserAdded) {
            showToast(MSG_REGISTRATION_FAILED);
            return;
        }

        showToast(MSG_ACCOUNT_CREATED);

        // Fetch and validate newly generated ID.
        int newUserId = db.getUserId(username, password);
        if (newUserId == INVALID_USER_ID) {
            showToast(MSG_REGISTRATION_FAILED);
            return;
        }

        // Transition user into inventory home page.
        Intent intent = new Intent(SignUpActivity.this, InventoryActivity.class);
        intent.putExtra(LoginActivity.EXTRA_USER_ID, newUserId);
        startActivity(intent);

        // Finish this screen context so clicking 'Back' from
        // inventory doesn't return to the Signup view.
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        // Ensure files/devices are left in the correct state upon program termination.
        if (db != null) {
            db.close();
        }
        super.onDestroy();
    }
}