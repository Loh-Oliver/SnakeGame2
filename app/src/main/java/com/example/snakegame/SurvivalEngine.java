package com.example.snakegame;

public class SurvivalEngine implements Runnable {
    private static final int POINTS_INTERVAL = 5000; // 5 seconds
    private static final int POINTS_PER_INTERVAL = 1; // Points to add per interval
    private static final int INITIAL_DELAY = 5000; // 5 seconds initial delay
    private static final Object lock = new Object();

    private boolean isRunning = false;

    Score score = new Score();

    public SurvivalEngine(Score score) {
        this.score = score;
    }

    public void start() {
            isRunning = true;
            new Thread(this).start();
    }

    public void stop() {
            isRunning = false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(INITIAL_DELAY); // Initial delay before adding points
            while (isRunning) {
                Thread.sleep(POINTS_INTERVAL); // Wait for the interval
                score.increaseScore();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
