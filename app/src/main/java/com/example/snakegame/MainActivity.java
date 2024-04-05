package com.example.snakegame;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;



public class MainActivity extends Activity implements SnakeEngine.SnakeDeathListener {

    // Declare an instance of SnakeEngine
    SnakeEngine snakeEngine;
    MediaPlayer mediaPlayer;
    Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the pixel dimensions of the screen
        display = getWindowManager().getDefaultDisplay();
        setContentView(R.layout.activity_main);

    }


    public void onSnakeDeath() {
        // Handle snake death here
        // For example, return to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
       // finish(); // Finish SnakeEngine activity
    }

    public void hideStatusBar() {
        // Hide the status bar using WindowInsetsController on Android 11 (API 30) and higher
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(WindowInsets.Type.statusBars());
        }
        // Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }



    public void play(View view) {
        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);
        // Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.crazyfrog);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        hideStatusBar();
        setContentView(snakeEngine);
        snakeEngine.resume();
        snakeEngine.newGame();
        // Set the death listener
        snakeEngine.setSnakeDeathListener(this);
    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();

        if (snakeEngine != null) {
            snakeEngine.resume();
             mediaPlayer.start();
        }
    }

    // Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();

//        mediaPlayer.release();
        if (snakeEngine != null) {
            snakeEngine.pause();
            mediaPlayer.pause();
        }
    }
}