package com.example.snakegame;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    /** A Firebase Authentication ennél rövidebb jelszót nem fogad el. */
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText emailEditText, passwordEditText, usernameEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword2);
        usernameEditText = findViewById(R.id.editTextText2);
        registerButton = findViewById(R.id.button);

        // Lásd MainActivity: google-services.json nélkül a Firebase hívásai dobnának.
        if (FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, R.string.firebase_missing, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String username = usernameEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, R.string.pleasefill_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.invalid_email));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordEditText.setError(getString(R.string.password_too_short, MIN_PASSWORD_LENGTH));
            return;
        }

        registerButton.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    registerButton.setEnabled(true);
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.regfail_format, errorMessage(task.getException())),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // A felhasználónevet a Realtime Database-be mentjük.
                        mDatabase.child("users").child(user.getUid()).child("username").setValue(username);
                    }

                    Toast.makeText(this, R.string.regsucc_name, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Login.class));
                    finish();
                });
    }

    /** A Firebase hibaüzenete néha hiányzik – ilyenkor általános szöveget mutatunk. */
    private String errorMessage(Exception exception) {
        String message = exception != null ? exception.getLocalizedMessage() : null;
        return TextUtils.isEmpty(message) ? getString(R.string.unknown_error) : message;
    }
}
