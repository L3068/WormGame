package com.example.wormgame;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class SkinChange extends AppCompatActivity {

    /**
     * A játék beállításait tároló SharedPreferences neve. Ez egy tárolási kulcs,
     * nem felhasználónak szánt név – átnevezésével a már telepített példányokon
     * elveszne a korábban kiválasztott kinézet, ezért maradt a régi értéken.
     */
    public static final String PREFS_NAME = "SnakeGamePrefs";
    /** A kiválasztott kinézet kulcsa. */
    public static final String KEY_SELECTED_SKIN = "selected_skin";

    public static final int SKIN_GREEN = 1;
    public static final int SKIN_RED = 2;
    public static final int SKIN_PURPLE = 3;

    private static final int SELECTED_COLOR = Color.parseColor("#8A8787");

    private ImageButton greenSkin, redSkin, purpleSkin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_change);

        greenSkin = findViewById(R.id.imageView);
        redSkin = findViewById(R.id.imageView2);
        purpleSkin = findViewById(R.id.imageView3);

        // Visszaállítjuk a korábban kiválasztott kinézetet.
        updateSelection(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_SELECTED_SKIN, SKIN_GREEN));

        greenSkin.setOnClickListener(v -> selectSkin(SKIN_GREEN));
        redSkin.setOnClickListener(v -> selectSkin(SKIN_RED));
        purpleSkin.setOnClickListener(v -> selectSkin(SKIN_PURPLE));
    }

    private void selectSkin(int skinId) {
        updateSelection(skinId);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(KEY_SELECTED_SKIN, skinId)
                .apply();
    }

    private void updateSelection(int selectedSkin) {
        greenSkin.setBackgroundColor(Color.TRANSPARENT);
        redSkin.setBackgroundColor(Color.TRANSPARENT);
        purpleSkin.setBackgroundColor(Color.TRANSPARENT);

        switch (selectedSkin) {
            case SKIN_RED:
                redSkin.setBackgroundColor(SELECTED_COLOR);
                break;
            case SKIN_PURPLE:
                purpleSkin.setBackgroundColor(SELECTED_COLOR);
                break;
            case SKIN_GREEN:
            default:
                greenSkin.setBackgroundColor(SELECTED_COLOR);
                break;
        }
    }
}
