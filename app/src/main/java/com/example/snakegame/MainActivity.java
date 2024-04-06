package com.example.snakegame;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.app.Activity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;


public class MainActivity extends Activity implements TrainGame.TrainDeathListener {

    // Declare an instance of SnakeEngine
    TrainGame trainEngine;
    MediaPlayer mediaPlayer;
    Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the pixel dimensions of the screen
        display = getWindowManager().getDefaultDisplay();
        setContentView(R.layout.activity_main);

        //Game name animation
        TextView myText =findViewById(R.id.myText);
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.sample_animation);
        myText.startAnimation(animation);

    }



    public void onTrainDeath() {
        // Handle snake death here
        // For example, return to MainActivity
        Intent intent = new Intent(this, EndingActivity.class);
        startActivity(intent);
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
        trainEngine = new TrainGame(this, size);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.crazyfrog);
        mediaPlayer.setVolume(0.25f, 0.25f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        hideStatusBar();
        setContentView(trainEngine);
        trainEngine.resume();
        trainEngine.newGame();
        // Set the death listener
        trainEngine.setTrainDeathListener(this);
    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();

        if (trainEngine != null) {
            trainEngine.resume();
             mediaPlayer.start();
        }
    }

    // Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();

//        mediaPlayer.release();
        if (trainEngine != null) {
            trainEngine.pause();
            mediaPlayer.pause();
        }
    }
}