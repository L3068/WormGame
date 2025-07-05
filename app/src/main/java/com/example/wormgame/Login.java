package com.example.snakegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        //Play
        Button play = findViewById(R.id.button3);
        play.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Wormgame.class);
            startActivity(intent);
        });

        //Skin change
        Button skin = findViewById(R.id.button4);
        skin.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SkinChange.class);
            startActivity(intent);
        });
    }
}