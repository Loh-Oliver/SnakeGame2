package com.example.snakegame;

public class Score {
    private int score = 0;
    private final Object lock = new Object();

    // Method to initialize the score
    public Score() {}

    // Method to increase the score
    public void increaseScore() {
        synchronized (lock) {
            score += 1;
        }
    }

    public void decreaseScore() {
        synchronized (lock) {
            score -= 1;
        }
    }

    public void resetScore() {
        synchronized (lock) {
            score = 0;
        }
    }

    // Method to retrieve the score
    public int getScore() {
            return score;
    }
}