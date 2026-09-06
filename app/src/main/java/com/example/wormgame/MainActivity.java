package com.example.wormgame;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.editTextText);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button5);
        Button register = findViewById(R.id.button2);
        Button forgotPassword = findViewById(R.id.forgot_password);

        // google-services.json nélkül nincs alapértelmezett FirebaseApp, és a
        // FirebaseAuth.getInstance() kivételt dobna. Ilyenkor összeomlás helyett
        // letiltjuk a bejelentkezést, és megmondjuk, mi hiányzik (lásd README).
        if (FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, R.string.firebase_missing, Toast.LENGTH_LONG).show();
            emailEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
            loginButton.setEnabled(false);
            register.setEnabled(false);
            forgotPassword.setEnabled(false);
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        register.setOnClickListener(v -> startActivity(new Intent(this, Register.class)));
        loginButton.setOnClickListener(v -> loginUser());
        forgotPassword.setOnClickListener(v -> resetPassword());
    }

    /**
     * Jelszó-visszaállító levél küldése a megadott e-mail címre. A Firebase
     * ismeretlen címnél is sikert jelez, hogy ne lehessen vele felhasználót
     * felderíteni – ezért az üzenet is semleges.
     */
    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError(getString(R.string.pleasefill_name));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.invalid_email));
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.reset_sent, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, getString(R.string.reset_failed_format,
                                errorMessage(task.getException())), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.pleasefill_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.invalid_email));
            return;
        }

        // Dupla kattintás ne indítson két bejelentkezést.
        loginButton.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.loginsucc_name, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Login.class));
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.logfail_format, errorMessage(task.getException())),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** A Firebase hibaüzenete néha hiányzik – ilyenkor általános szöveget mutatunk. */
    private String errorMessage(Exception exception) {
        String message = exception != null ? exception.getLocalizedMessage() : null;
        return TextUtils.isEmpty(message) ? getString(R.string.unknown_error) : message;
    }
}
