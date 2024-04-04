package com.example.snakegame;

public class Score {
    private int score = 0;
    private final Object lock = new Object();

    // Method to initialize the score
    public Score() {}

    // Method to increase the score
    public void increaseScore() {
        synchronized (lock) {
            score = score + 1;
        }
    }

    // Method to retrieve the score
    public int getScore() {
            return score;
    }
}