package com.example.producerconsumer;

import me.tongfei.progressbar.ProgressBar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple helper that visualizes the current queue size using a progress bar.
 */
public class QueueVisualizer {

    private final ProgressBar progressBar;
    private final AtomicInteger currentSize = new AtomicInteger();
    private final int capacity;

    public QueueVisualizer(int capacity) {
        this.capacity = capacity;
        this.progressBar = new ProgressBar("Queue", capacity);
        this.progressBar.stepTo(0);
    }

    public synchronized void update(int currentSize) {
        this.currentSize.set(currentSize);
        progressBar.stepTo(currentSize);
    }

    public int getCurrentSize() {
        return currentSize.get();
    }

    public int getCapacity() {
        return capacity;
    }

    public synchronized void close() {
        progressBar.close();
    }
}
