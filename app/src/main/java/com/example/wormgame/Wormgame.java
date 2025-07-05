package com.example.snakegame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Wormgame extends Activity {

    private RelativeLayout board, border;
    private LinearLayout lilu;
    private Button upButton, downButton, leftButton, rightButton, pauseButton, newgame, resume, playagain, score, score2;
    private ImageView meat, snake;
    private List<ImageView> snakeSegments = new ArrayList<>();
    private Handler handler = new Handler();
    private long delayMillis = 30;
    private String currentDirection = "right";
    private String resumeDiection = "right";
    private int scorex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wormgame);

        board = findViewById(R.id.board);
        border = findViewById(R.id.relativeLayout);
        lilu = findViewById(R.id.lilu);
        upButton = findViewById(R.id.up);
        downButton = findViewById(R.id.down);
        leftButton = findViewById(R.id.left);
        rightButton = findViewById(R.id.right);
        pauseButton = findViewById(R.id.pause);
        newgame = findViewById(R.id.new_game);
        resume = findViewById(R.id.resume);
        playagain = findViewById(R.id.playagain);
        score = findViewById(R.id.score);
        score2 = findViewById(R.id.score2);

        board.setVisibility(View.INVISIBLE);
        playagain.setVisibility(View.INVISIBLE);
        score.setVisibility(View.INVISIBLE);
        score2.setVisibility(View.INVISIBLE);

        newgame.setOnClickListener(v -> board.post(() -> startGame()));

        upButton.setOnClickListener(v -> currentDirection = "up");
        downButton.setOnClickListener(v -> currentDirection = "down");
        leftButton.setOnClickListener(v -> currentDirection = "left");
        rightButton.setOnClickListener(v -> currentDirection = "right");
        pauseButton.setOnClickListener(v -> {
            resumeDiection = currentDirection;
            currentDirection = "pause";
            board.setVisibility(View.INVISIBLE);
            newgame.setVisibility(View.VISIBLE);
            resume.setVisibility(View.VISIBLE);
        });
        resume.setOnClickListener(v -> {
            currentDirection = resumeDiection;
            board.setVisibility(View.VISIBLE);
            newgame.setVisibility(View.INVISIBLE);
            resume.setVisibility(View.INVISIBLE);
        });
        playagain.setOnClickListener(v -> recreate());
    }

    private void startGame() {
        SharedPreferences prefs = getSharedPreferences("SnakeGamePrefs", MODE_PRIVATE);
        int selectedSkin = prefs.getInt("selected_skin", 1);
        int skinDrawable;

        switch (selectedSkin) {
            case 1:
                skinDrawable = R.drawable.worm;
                break;
            case 2:
                skinDrawable = R.drawable.worm_red;
                break;
            case 3:
                skinDrawable = R.drawable.worm_purple;
                break;
            default:
                skinDrawable = R.drawable.worm;
                break;
        }

        board.setVisibility(View.VISIBLE);
        newgame.setVisibility(View.INVISIBLE);
        resume.setVisibility(View.INVISIBLE);
        score2.setVisibility(View.VISIBLE);

        snake = new ImageView(this);
        snake.setImageResource(skinDrawable);
        snake.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        board.addView(snake);
        snakeSegments.add(snake);
        snake.setX(100);
        snake.setY(100);


        meat = new ImageView(this);
        meat.setImageResource(R.drawable.meat);
        meat.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        board.addView(meat);

        Random random = new Random();
        meat.setX(random.nextInt(381));
        meat.setY(random.nextInt(381));


        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                float snakeX = snake.getX();
                float snakeY = snake.getY();
                for (int i = snakeSegments.size() - 1; i > 0; i--) {
                    snakeSegments.get(i).setX(snakeSegments.get(i - 1).getX());
                    snakeSegments.get(i).setY(snakeSegments.get(i - 1).getY());
                }

                switch (currentDirection) {
                    case "up":
                        snakeY -= 10;
                        if (snakeY < 0) endGame();
                        snake.setY(snakeY);
                        break;
                    case "down":
                        snakeY += 10;
                        if (snakeY + snake.getHeight() > board.getHeight())
                        {
                            endGame();
                        }
                        snake.setY(snakeY);
                        break;
                    case "left":
                        snakeX -= 10;
                        if (snakeX < 0) endGame();
                        snake.setX(snakeX);
                        break;
                    case "right":
                        snakeX += 10;
                        if (snakeX + snake.getHeight() > board.getHeight())
                        {
                            endGame();
                        }
                        snake.setX(snakeX);
                        break;
                }

                float distance = (float) Math.sqrt(Math.pow(snake.getX() - meat.getX(), 2) + Math.pow(snake.getY() - meat.getY(), 2));
                if (distance < 50) {
                    ImageView newSegment = new ImageView(Wormgame.this);
                    newSegment.setImageResource(skinDrawable);
                    newSegment.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    board.addView(newSegment);
                    snakeSegments.add(newSegment);

                    meat.setX(random.nextInt(381));
                    meat.setY(random.nextInt(381));

                    delayMillis--;
                    scorex++;
                    score2.setText(getString(R.string.button_score_name) + ": " + scorex);
                }

                handler.postDelayed(this, delayMillis);
            }

            private void endGame() {
                border.setBackgroundColor(ContextCompat.getColor(Wormgame.this, R.color.red));
                playagain.setVisibility(View.VISIBLE);
                currentDirection = getString(R.string.button_pause_name);
                lilu.setVisibility(View.INVISIBLE);
                score.setText(getString(R.string.yourscore_name) + scorex);
                score.setVisibility(View.VISIBLE);
                score2.setVisibility(View.INVISIBLE);
            }
        };

        handler.postDelayed(runnable, delayMillis);
    }
}
