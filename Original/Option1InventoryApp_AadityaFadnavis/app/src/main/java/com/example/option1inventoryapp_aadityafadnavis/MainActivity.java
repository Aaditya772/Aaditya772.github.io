package com.example.option1inventoryapp_aadityafadnavis;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText usernameInput, passwordInput;
    Button loginButton;

    // Initial setup.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database and link Java variables to input fields/button.
        db = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);

        // Login logic.
        loginButton.setOnClickListener(v -> {
            String user = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();

            if(user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = db.getUserId(user, pass);

            if(userId != -1) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                navigateToInventory(userId);
            } else if (db.checkUsernameExists(user)) {
                Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
            } else {
                if(db.addUser(user, pass)) {
                    int newUserId = db.getUserId(user, pass);
                    Toast.makeText(this, "New Account Created!", Toast.LENGTH_SHORT).show();
                    navigateToInventory(newUserId);
                }
            }
        });
    }

    // Login to inventory screen for specific user.
    private void navigateToInventory(int userId) {
        Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }
}