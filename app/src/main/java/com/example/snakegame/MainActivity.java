package com.example.snakegame;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;


public class MainActivity extends Activity {

    // Declare an instance of SnakeEngine
    SnakeEngine snakeEngine;
    Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the pixel dimensions of the screen
        display = getWindowManager().getDefaultDisplay();
        setContentView(R.layout.activity_main);
        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size);

    }

    public void play(View view) {
        setContentView(snakeEngine);
    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
    }

    // Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();
    }
}