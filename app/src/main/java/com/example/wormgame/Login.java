package com.example.wormgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/** Bejelentkezés utáni főmenü: játék indítása és kinézet választás. */
public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button play = findViewById(R.id.button3);
        play.setOnClickListener(v -> startActivity(new Intent(this, Wormgame.class)));

        Button skin = findViewById(R.id.button4);
        skin.setOnClickListener(v -> startActivity(new Intent(this, SkinChange.class)));

        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> logout());
    }

    /** Kijelentkezés után a bejelentkező képernyőre térünk vissza, a hátteret kiürítve. */
    private void logout() {
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseAuth.getInstance().signOut();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
