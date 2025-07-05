package com.example.snakegame;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SkinChange extends AppCompatActivity {
    ImageView imageView1, imageView2, imageView3;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_change);

        prefs = getSharedPreferences("SnakeGamePrefs", MODE_PRIVATE);
        editor = prefs.edit();

        imageView1 = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);

        // Visszaállítjuk a kiválasztott hátteret
        int selectedSkin = prefs.getInt("selected_skin", 1);
        updateSelection(selectedSkin);

        // Kattintáskezelők
        imageView1.setOnClickListener(v -> selectSkin(1));
        imageView2.setOnClickListener(v -> selectSkin(2));
        imageView3.setOnClickListener(v -> selectSkin(3));
    }

    private void selectSkin(int skinId) {
        updateSelection(skinId);
        editor.putInt("selected_skin", skinId);
        editor.apply();
    }

    private void updateSelection(int selectedSkin) {
        imageView1.setBackgroundColor(Color.TRANSPARENT);
        imageView2.setBackgroundColor(Color.TRANSPARENT);
        imageView3.setBackgroundColor(Color.TRANSPARENT);

        switch (selectedSkin) {
            case 1:
                imageView1.setBackgroundColor(Color.parseColor("#8A8787"));
                break;
            case 2:
                imageView2.setBackgroundColor(Color.parseColor("#8A8787"));
                break;
            case 3:
                imageView3.setBackgroundColor(Color.parseColor("#8A8787"));
                break;
        }
    }

}