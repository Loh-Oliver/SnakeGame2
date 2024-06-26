package com.example.snakegame;

public class PassengerDecay implements Runnable {
    private static final int POINTS_INTERVAL = 7000; // 7 seconds
    private static final int POINTS_PER_INTERVAL = 1; // Points to decrease per interval
    private static final Object lock = new Object();

    private boolean isRunning = false;

    private long decayStart;

    float timeRatio;

    Score score;
    Thread thread;

    public PassengerDecay(Score score) {
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
                decayStart = System.currentTimeMillis();
                Thread.sleep(POINTS_INTERVAL); // Wait for the interval
                score.decreaseScore();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public float getTimeRatio() {
        long timeElapsed = System.currentTimeMillis() - decayStart;
        long timeLeft = POINTS_INTERVAL - timeElapsed;
        timeRatio = (float) timeLeft / POINTS_INTERVAL * 100;
        return timeRatio;
    }
}
