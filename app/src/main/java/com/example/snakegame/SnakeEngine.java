package com.example.snakegame;

import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.os.Vibrator;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
class SnakeEngine extends SurfaceView implements Runnable {

    // Our game thread for the main game loop
    private Thread thread = null;

    // To hold a reference to the Activity
    private Context context;

    // for playing sound effects
    private SoundPool soundPool;
    private int eat_bob = -1;
    private int snake_crash = -1;

    // For tracking movement Heading
    public enum Heading {UP, RIGHT, DOWN, LEFT}
    // Start by heading to the right
    private Heading heading = Heading.RIGHT;

    // To hold the screen size in pixels
    private int screenX;
    private int screenY;

    // How long is the snake
    private int snakeLength;

    // Where is Bob hiding?
    private int bobX;
    private int bobY;

    // The size in pixels of a snake segment
    private int blockSize;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 20;
    private int numBlocksHigh;

    // Control pausing between updates
    private long nextFrameTime;
    // Update the game 10 times per second
    private final long FPS = 5;
    // There are 1000 milliseconds in a second
    private final long MILLIS_PER_SECOND = 1000;
// We will draw the frame much more often

    // How many points does the player have
    private Score scoreManager;

    // survival
    private SurvivalEngine survivalEngine;

    // The location in the grid of all the segments
    private int[] snakeXs;
    private int[] snakeYs;

    // Everything we need for drawing
// Is the game currently playing?
    private volatile boolean isPlaying;

    // A canvas for our paint
    private Canvas canvas;

    // Required to use canvas
    private SurfaceHolder surfaceHolder;

    // Some paint for our canvas
    private Paint paint;

    // Declare Vibrator object
    private Vibrator vibrator;

    public SnakeEngine(Context context, Point size) {
        super(context);

        // Initialize Vibrator
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Choose the minimum of width and height as the screen size to make the playing area square
        screenX = size.x;
        screenY = size.x + 500;

        // Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE;
        // Calculate the number of blocks high to maintain aspect ratio
        numBlocksHigh = screenY / blockSize;

        // Set the sound up
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        try {
            // Create objects of the 2 required classes
            // Use m_Context because this is a reference to the Activity
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the two sounds in memory
            descriptor = assetManager.openFd("pick_up_MRT.ogg");
            eat_bob = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("CrashSound.ogg");
            snake_crash = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Error
            System.out.println("cannot retrieve game sounds");
        }


        // Initialize the drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // If you score 200 you are rewarded with a crash achievement!
        snakeXs = new int[200];
        snakeYs = new int[200];

        // initialize score
        scoreManager = new Score();

    }

    @Override
    public void run() {
        survivalEngine = new SurvivalEngine(scoreManager);
        survivalEngine.start();
        while (isPlaying) {
            // Update 10 times a second
            if(updateRequired()) {
                update();
                draw();
            }
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
            survivalEngine.stop();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {
        // Start with a single snake segment
        snakeLength = 1;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        // Get Bob ready for dinner
        spawnBob();

        // Reset the score
        scoreManager.resetScore();

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    public synchronized void addScore() {
        scoreManager.increaseScore();
    }

    public void spawnBob() {
        Random random = new Random();
        bobX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        bobY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void eatBob(){
        // Increase the size of the snake
        snakeLength++;
        //replace Bob
        spawnBob();
        //add to the score
        addScore();
//        soundPool.play(eat_bob, 1, 1, 0, 0, 1);
        vibrator.vibrate(500);
        survivalEngine.restart();
    }

    private void moveSnake(){
        // Move the body
        for (int i = snakeLength; i > 0; i--) {
            // Start at the back and move it
            // to the position of the segment in front of it
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];

            // Exclude the head because
            // the head has nothing in front of it
        }

        // Move the head in the appropriate heading
        switch (heading) {
            case UP:
                snakeYs[0]--;
                break;

            case RIGHT:
                snakeXs[0]++;
                break;

            case DOWN:
                snakeYs[0]++;
                break;

            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath(){
        // Has the snake died?
        boolean dead = false;

        // Hit the screen edge
        if (snakeXs[0] == -1) dead = true;
        if (snakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (snakeYs[0] == -1) dead = true;
        if (snakeYs[0] == numBlocksHigh) dead = true;

        // Eaten itself?
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                dead = true;
            }
        }

        return dead;
    }


    public void draw() {
        // Get a lock on the canvas
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Draw the playing area
            canvas.drawColor(Color.BLACK); // Choose your desired background color
            int playingAreaLeft = 0;
            int playingAreaTop = 200; // Adjust this value to shift the playing area lower
            int playingAreaRight = screenX;
            int playingAreaBottom = screenY + 200; // Adjust this value to shift the playing area lower
            paint.setColor(Color.argb(255, 150, 75, 0));
            canvas.drawRect(playingAreaLeft, playingAreaTop, playingAreaRight, playingAreaBottom, paint);

            // Draw the snake
            paint.setColor(Color.argb(255, 255, 255, 255));
            for (int i = 0; i < snakeLength; i++) {
                canvas.drawRoundRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize) + playingAreaTop,
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize + playingAreaTop,
                        20,20, paint);
            }

            // Draw Bob
            paint.setColor(Color.argb(255, 255, 253, 208));
            canvas.drawRect(bobX * blockSize,
                    (bobY * blockSize) + playingAreaTop,
                    (bobX * blockSize) + blockSize,
                    (bobY * blockSize) + blockSize + playingAreaTop,
                    paint);

            // Draw the score
            paint.setColor(Color.WHITE);
            paint.setTextSize(90);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Set bold typeface
            String scoreText = "Passenger: " + scoreManager.getScore();
            float textWidth = paint.measureText(scoreText); // Measure text width to center it
            canvas.drawText(scoreText, (screenX - textWidth) / 2, 150, paint);

            // Draw the "L" and "R" buttons

            paint.setTextSize(120); // Increase text size for bigger buttons
            canvas.drawText("L", screenX / 4 - 60, playingAreaBottom + 200, paint);
            canvas.drawText("R", screenX * 3 / 4 - 20, playingAreaBottom + 200, paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void update() {
        // Did the head of the snake eat Bob?
        if (snakeXs[0] == bobX && snakeYs[0] == bobY) {
            eatBob();
        }

        moveSnake();

        if (detectDeath()) {
            //start again
            soundPool.play(snake_crash, 1, 1, 0, 0, 1);

            newGame();
        }

        snakeLength = Math.max(1, scoreManager.getScore() + 1);
        if (scoreManager.getScore() < 0) {
            // Restart the game if score turns negative
            soundPool.play(snake_crash, 1, 1, 0, 0, 1);
            newGame();
        }
    }

    public boolean updateRequired() {

        // Are we due to update the frame
        if(nextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            nextFrameTime =System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                // Check if the touch event is within the "L" button bounds
                if (touchX >= 0 && touchX < screenX / 2 && touchY > screenY + 200) {
                    // Move left
                    switch (heading) {
                        case UP:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.UP;
                            break;
                    }
                }
                // Check if the touch event is within the "R" button bounds
                else if (touchX >= screenX / 2 && touchX < screenX && touchY > screenY + 200) {
                    // Move right
                    switch (heading) {
                        case UP:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.UP;
                            break;
                    }
                }
                break;
        }
        return true;
    }
}
