package com.example.producerconsumer;

import me.tongfei.progressbar.ProgressBar;

/**
 * Simple helper that visualizes the current queue size using a progress bar.
 */
public class QueueVisualizer {

    private final ProgressBar progressBar;

    public QueueVisualizer(int capacity) {
        this.progressBar = new ProgressBar("Queue", capacity);
        this.progressBar.stepTo(0);
    }

    public synchronized void update(int currentSize) {
        progressBar.stepTo(currentSize);
    }

    public synchronized void close() {
        progressBar.close();
    }
}
