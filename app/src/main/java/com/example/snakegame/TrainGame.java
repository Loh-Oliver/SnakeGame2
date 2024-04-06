package com.example.snakegame;

import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.os.Vibrator;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
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
class TrainGame extends SurfaceView implements Runnable {

    // Our game thread for the main game loop
    private Thread thread = null;

    // To hold a reference to the Activity
    private Context context;

    // for playing sound effects
    private SoundPool soundPool;
    private int pick_passenger = -1;
    private int train_crash = -1;

    // For tracking movement Heading
    public enum Heading {UP, RIGHT, DOWN, LEFT}
    // Start by heading to the right
    private Heading heading = Heading.RIGHT;

    // To hold the screen size in pixels
    private int screenX;
    private int screenY;

    // How long is the train
    private int trainLength;

    // Where is Bob hiding?
    private int bobX;
    private int bobY;

    // The size in pixels of a train segment
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
    private PassengerDecay survivalEngine;

    // The location in the grid of all the segments
    private int[] trainXs;
    private int[] trainYs;

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


    public TrainGame(Context context, Point size) {

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
            pick_passenger = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("CrashSound.ogg");
            train_crash = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Error
            System.out.println("cannot retrieve game sounds");
        }


        // Initialize the drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // If you score 200 you are rewarded with a crash achievement!
        trainXs = new int[200];
        trainYs = new int[200];

        // initialize score
        scoreManager = new Score();

    }

    @Override
    public void run() {
        survivalEngine = new PassengerDecay(scoreManager);
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
        // Start with a single train segment
        trainLength = 1;
        trainXs[0] = NUM_BLOCKS_WIDE / 2;
        trainYs[0] = numBlocksHigh / 2;

        // Get Passenger ready to be picked up
        spawnPassenger();

        // Reset the score
        scoreManager.resetScore();

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    public synchronized void addScore() {
        scoreManager.increaseScore();
    }

    public void spawnPassenger() {
        Random random = new Random();
        bobX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        bobY = random.nextInt(numBlocksHigh - 1) + 1;
    }



    private void pickPassenger(){
        // Increase the size of the snake
        trainLength++;
        //replace Bob
        spawnPassenger();
        //add to the score
        addScore();
        soundPool.play(pick_passenger, 1.5f, 1.5f, 0, 0, 1);
        vibrator.vibrate(500);
        survivalEngine.restart();
    }

    private void moveTrain(){
        // Move the body
        for (int i = trainLength; i > 0; i--) {
            // Start at the back and move it
            // to the position of the segment in front of it
            trainXs[i] = trainXs[i - 1];
            trainYs[i] = trainYs[i - 1];

            // Exclude the head because
            // the head has nothing in front of it
        }

        // Move the head in the appropriate heading
        switch (heading) {
            case UP:
                trainYs[0]--;
                break;

            case RIGHT:
                trainXs[0]++;
                break;

            case DOWN:
                trainYs[0]++;
                break;

            case LEFT:
                trainXs[0]--;
                break;
        }
    }

    public interface TrainDeathListener {
        void onTrainDeath();
    }

    // Add a member variable to hold the listener reference
    private TrainDeathListener deathListener;

    // Setter method for the listener
    public void setTrainDeathListener(TrainDeathListener listener) {
        this.deathListener = listener;
    }

    private boolean detectDeath(){
        // Has the train died?
        boolean dead = false;

        // Hit the screen edge
        if (trainXs[0] == -1) dead = true;
        if (trainXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (trainYs[0] == -1) dead = true;
        if (trainYs[0] == numBlocksHigh) dead = true;

        // Eaten itself?
        for (int i = trainLength - 1; i > 0; i--) {
            if ((trainXs[0] == trainXs[i]) && (trainYs[0] == trainYs[i])) {

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
            for (int i = 0; i < trainLength; i++) {
                canvas.drawRoundRect(trainXs[i] * blockSize,
                        (trainYs[i] * blockSize) + playingAreaTop,
                        (trainXs[i] * blockSize) + blockSize,
                        (trainYs[i] * blockSize) + blockSize + playingAreaTop,
                        20,20, paint);
            }

// Draw Bob
            paint.setColor(Color.argb(255, 255, 253, 208));
// Draw head
            canvas.drawCircle((bobX * blockSize) + (blockSize / 2),
                    (bobY * blockSize) + (blockSize / 2) + playingAreaTop - (blockSize / 4),
                    blockSize / 4,
                    paint);
// Draw body
            canvas.drawRect((bobX * blockSize) + (blockSize / 3),
                    (bobY * blockSize) + (blockSize / 2) + playingAreaTop,
                    (bobX * blockSize) + (blockSize * 2 / 3),
                    (bobY * blockSize) + (blockSize * 2) + playingAreaTop,
                    paint);
// Draw arms
            canvas.drawRect((bobX * blockSize),
                    (bobY * blockSize) + (blockSize / 2) + playingAreaTop,
                    (bobX * blockSize) + (blockSize / 3),
                    (bobY * blockSize) + (blockSize * 2) + playingAreaTop,
                    paint);
            canvas.drawRect((bobX * blockSize) + (blockSize * 2 / 3),
                    (bobY * blockSize) + (blockSize / 2) + playingAreaTop,
                    (bobX * blockSize) + blockSize,
                    (bobY * blockSize) + (blockSize * 2) + playingAreaTop,
                    paint);
// Draw legs
            canvas.drawRect((bobX * blockSize) + (blockSize / 3),
                    (bobY * blockSize) + (blockSize * 2) + playingAreaTop,
                    (bobX * blockSize) + (blockSize / 2),
                    (bobY * blockSize) + (blockSize * 3) + playingAreaTop,
                    paint);
            canvas.drawRect((bobX * blockSize) + (blockSize / 2),
                    (bobY * blockSize) + (blockSize * 2) + playingAreaTop,
                    (bobX * blockSize) + (blockSize * 2 / 3),
                    (bobY * blockSize) + (blockSize * 3) + playingAreaTop,
                    paint);

            // Draw the score
            paint.setColor(Color.WHITE);
            paint.setTextSize(90);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Set bold typeface
            String scoreText = "Passenger: " + scoreManager.getScore();
            float textWidth = paint.measureText(scoreText); // Measure text width to center it
            canvas.drawText(scoreText, (screenX - textWidth) / 2, 150, paint);

            drawProgressBar(canvas, survivalEngine.getTimeRatio());

            // Draw the "L" and "R" buttons

            paint.setTextSize(120); // Increase text size for bigger buttons
            canvas.drawText("L", screenX / 4 - 60, playingAreaBottom + 200, paint);
            canvas.drawText("R", screenX * 3 / 4 - 20, playingAreaBottom + 200, paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    // Add a method to draw a progress bar
    public void drawProgressBar(Canvas canvas, float progress) {
        // Calculate the dimensions of the progress bar
        int progressBarWidth = (int) (screenX * 0.8);
        int progressBarHeight = 30;
        int progressBarX = (screenX - progressBarWidth) / 2;
        int progressBarY = 50;

        // Draw the background of the progress bar
        paint.setColor(Color.GRAY);
        canvas.drawRect(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + progressBarHeight, paint);

        // Calculate the filled portion of the progress bar based on the progress value
        int filledWidth = (int) ((progress / 100.0) * progressBarWidth);

        // Draw the filled portion of the progress bar
        paint.setColor(Color.GREEN);
        canvas.drawRect(progressBarX, progressBarY, progressBarX + filledWidth, progressBarY + progressBarHeight, paint);
    }

    public void update() {
        // Did the head of the snake eat Bob?
        if (trainXs[0] == bobX && trainYs[0] == bobY) {
            pickPassenger();
        }

        moveTrain();

        if (detectDeath()) {
            //start again
            soundPool.play(train_crash, 3, 3, 0, 0, 1);

           // newGame();
            if (deathListener != null) {
                deathListener.onTrainDeath();
            }
        }

        trainLength = Math.max(1, scoreManager.getScore() + 1);
        if (scoreManager.getScore() < 0) {
            // Restart the game if score turns negative
            soundPool.play(train_crash, 3, 3, 0, 0, 1);
           // newGame();
            if (deathListener != null) {
                deathListener.onTrainDeath();
            }
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
