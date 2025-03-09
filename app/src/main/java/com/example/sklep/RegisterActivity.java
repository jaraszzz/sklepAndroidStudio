package com.example.sklep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText email, username, password;
    Button registerButton, loginRedirectButton;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        loginRedirectButton = findViewById(R.id.loginRedirectButton);
        dbHelper = new DatabaseHelper(this);

        registerButton.setOnClickListener(v -> {
            String emailText = email.getText().toString();
            String usernameText = username.getText().toString();
            String passwordText = password.getText().toString();

            if (email.getText().toString().isEmpty()) {
                email.setText(getString(R.string.empty_field));
                email.requestFocus();
            }
            else if (username.getText().toString().isEmpty()){
                username.setError(getString(R.string.empty_field));
                username.requestFocus();
            }
            else if (password.getText().toString().isEmpty()) {
                password.setError(getString(R.string.empty_field));
                password.requestFocus();
            }
            else {
                boolean registered = dbHelper.registerUser(emailText, usernameText, passwordText);
                if (registered) {
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration Failed! User may already exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginRedirectButton.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }
}
