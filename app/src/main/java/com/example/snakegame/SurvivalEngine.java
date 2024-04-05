package com.example.snakegame;

public class SurvivalEngine implements Runnable {
    private static final int POINTS_INTERVAL = 5000; // 10 seconds
    private static final int POINTS_PER_INTERVAL = 1; // Points to decrease per interval
    private static final Object lock = new Object();

    private boolean isRunning = false;

    Score score;
    Thread thread;

    public SurvivalEngine(Score score) {
        this.score = score;
    }

    public void start() {
            isRunning = true;
            thread = new Thread(this);
            thread.start();
    }

    public void stop() {
            isRunning = false;
            thread.interrupt();
    }

    public void restart() {
        stop();
        start();
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                Thread.sleep(POINTS_INTERVAL); // Wait for the interval
                score.decreaseScore();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
