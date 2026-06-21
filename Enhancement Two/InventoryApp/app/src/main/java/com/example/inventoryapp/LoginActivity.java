/*
 * File: LoginActivity.java
 * Description: Handles logic for the initial login page.
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

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "com.example.inventoryapp.USER_ID";

    // User facing message constants.
    private static final String MSG_EMPTY_FIELDS = "Please enter all fields";
    private static final String MSG_LOGIN_SUCCESS = "Login Successful";
    private static final String MSG_INVALID_PASSWORD = "Invalid Password";
    private static final String MSG_ACCOUNT_CREATED = "New Account Created!";
    private static final String MSG_REGISTRATION_FAILED = "Error creating account. Please try again.";

    // Private access modifiers enforced for proper encapsulation.
    private DatabaseHelper db;
    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Hide action bar for good appearance.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configure system windows for dark status bar icons for visibility on the light background.
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        // Initialize dependencies and view bindings.
        db = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);

        // Setup unified validation, login, and registration branch logic.
        loginButton.setOnClickListener(v -> handleLoginOrRegistration());

        Button createAccountButton = findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    // Extract inputs, validate presence, and process authentication.
    private void handleLoginOrRegistration() {
        String user = usernameInput.getText().toString().trim();
        String pass = passwordInput.getText().toString().trim();

        // Validation branch.
        if (user.isEmpty() || pass.isEmpty()) {
            showToast(MSG_EMPTY_FIELDS);
            return;
        }

        int userId = db.getUserId(user, pass);

        // Existing user authentication branch.
        if (userId != -1) {
            showToast(MSG_LOGIN_SUCCESS);
            navigateToInventory(userId);
            return;
        }

        // Credential error branch.
        if (db.checkUsernameExists(user)) {
            showToast(MSG_INVALID_PASSWORD);
            return;
        } else {
            showToast("Account does not exist. Please create a new account.");
        }

    }

    private void navigateToInventory(int userId) {
        Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}