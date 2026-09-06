package com.example.wormgame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Wormgame extends Activity {

    /** A pálya ennyi cella széles és magas. */
    private static final int GRID_SIZE = 19;

    private RelativeLayout board, border;
    private LinearLayout lilu;
    private Button newgame, resume, playagain, score, score2;

    private final List<ImageView> wormSegments = new ArrayList<>();
    private ImageView meat;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WormEngine engine;
    private int skinDrawable = R.drawable.worm;
    private int cellSize;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            engine.step();
            render();
            if (engine.isGameOver()) {
                showGameOver();
            } else {
                handler.postDelayed(this, engine.delayMillis());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wormgame);

        board = findViewById(R.id.board);
        border = findViewById(R.id.relativeLayout);
        lilu = findViewById(R.id.lilu);
        Button upButton = findViewById(R.id.up);
        Button downButton = findViewById(R.id.down);
        Button leftButton = findViewById(R.id.left);
        Button rightButton = findViewById(R.id.right);
        Button pauseButton = findViewById(R.id.pause);
        newgame = findViewById(R.id.new_game);
        resume = findViewById(R.id.resume);
        playagain = findViewById(R.id.playagain);
        score = findViewById(R.id.score);
        score2 = findViewById(R.id.score2);

        engine = new WormEngine(GRID_SIZE, GRID_SIZE);

        board.setVisibility(View.INVISIBLE);
        playagain.setVisibility(View.INVISIBLE);
        score.setVisibility(View.INVISIBLE);
        score2.setVisibility(View.INVISIBLE);
        resume.setVisibility(View.INVISIBLE);

        newgame.setOnClickListener(v -> board.post(this::startGame));

        upButton.setOnClickListener(v -> engine.requestDirection(WormEngine.Direction.UP));
        downButton.setOnClickListener(v -> engine.requestDirection(WormEngine.Direction.DOWN));
        leftButton.setOnClickListener(v -> engine.requestDirection(WormEngine.Direction.LEFT));
        rightButton.setOnClickListener(v -> engine.requestDirection(WormEngine.Direction.RIGHT));
        pauseButton.setOnClickListener(v -> pauseGame());
        resume.setOnClickListener(v -> resumeGame());
        playagain.setOnClickListener(v -> startGame());
    }

    private void startGame() {
        handler.removeCallbacks(tick);

        SharedPreferences prefs = getSharedPreferences(SkinChange.PREFS_NAME, MODE_PRIVATE);
        switch (prefs.getInt(SkinChange.KEY_SELECTED_SKIN, SkinChange.SKIN_GREEN)) {
            case SkinChange.SKIN_RED:
                skinDrawable = R.drawable.worm_red;
                break;
            case SkinChange.SKIN_PURPLE:
                skinDrawable = R.drawable.worm_purple;
                break;
            case SkinChange.SKIN_GREEN:
            default:
                skinDrawable = R.drawable.worm;
                break;
        }

        // Az előző menet nézetei nem maradhatnak a pályán.
        board.removeAllViews();
        wormSegments.clear();
        meat = null;

        cellSize = Math.max(1, Math.min(board.getWidth(), board.getHeight()) / GRID_SIZE);
        engine.reset();
        engine.start();

        border.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        board.setVisibility(View.VISIBLE);
        lilu.setVisibility(View.VISIBLE);
        newgame.setVisibility(View.INVISIBLE);
        resume.setVisibility(View.INVISIBLE);
        playagain.setVisibility(View.INVISIBLE);
        score.setVisibility(View.INVISIBLE);
        score2.setVisibility(View.VISIBLE);

        render();
        handler.postDelayed(tick, engine.delayMillis());
    }

    private void pauseGame() {
        if (engine.state() != WormEngine.State.RUNNING) {
            return;
        }
        engine.pause();
        handler.removeCallbacks(tick);
        board.setVisibility(View.INVISIBLE);
        newgame.setVisibility(View.VISIBLE);
        resume.setVisibility(View.VISIBLE);
    }

    private void resumeGame() {
        if (engine.state() != WormEngine.State.PAUSED) {
            return;
        }
        engine.start();
        board.setVisibility(View.VISIBLE);
        newgame.setVisibility(View.INVISIBLE);
        resume.setVisibility(View.INVISIBLE);
        handler.postDelayed(tick, engine.delayMillis());
    }

    /** Az aktuális állapot kirajzolása: cellánként egy-egy ImageView. */
    private void render() {
        List<WormEngine.Cell> cells = engine.body();

        while (wormSegments.size() < cells.size()) {
            ImageView segment = new ImageView(this);
            segment.setImageResource(skinDrawable);
            board.addView(segment, new RelativeLayout.LayoutParams(cellSize, cellSize));
            wormSegments.add(segment);
        }
        while (wormSegments.size() > cells.size()) {
            ImageView segment = wormSegments.remove(wormSegments.size() - 1);
            board.removeView(segment);
        }
        for (int i = 0; i < cells.size(); i++) {
            place(wormSegments.get(i), cells.get(i));
        }

        WormEngine.Cell foodCell = engine.food();
        if (foodCell != null) {
            if (meat == null) {
                meat = new ImageView(this);
                meat.setImageResource(R.drawable.meat);
                board.addView(meat, new RelativeLayout.LayoutParams(cellSize, cellSize));
            }
            place(meat, foodCell);
        } else if (meat != null) {
            board.removeView(meat);
            meat = null;
        }

        score2.setText(getString(R.string.score_format, engine.score()));
    }

    private void place(ImageView view, WormEngine.Cell cell) {
        view.setX(cell.x * cellSize);
        view.setY(cell.y * cellSize);
    }

    private void showGameOver() {
        handler.removeCallbacks(tick);
        border.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        score.setText(getString(R.string.yourscore_format, engine.score()));
        score.setVisibility(View.VISIBLE);
        playagain.setVisibility(View.VISIBLE);
        // A pálya a testvérnézetek fölé rajzolódik, ezért a játék vége panelt
        // előre kell hozni – enélkül a fekete tábla takarja el.
        score.bringToFront();
        playagain.bringToFront();
        lilu.setVisibility(View.INVISIBLE);
        score2.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Háttérben ne fusson tovább a játék, és ne szivárogjon a Handler.
        pauseGame();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(tick);
        super.onDestroy();
    }
}
