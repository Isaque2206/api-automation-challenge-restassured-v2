package com.isaque.qa.util;

import java.time.Duration;
import java.util.concurrent.Semaphore;

public class RateLimiter {
    private final long intervalMs;
    private long lastTime = 0L;
    private final Semaphore sem = new Semaphore(1);

    public RateLimiter(Duration interval) {
        this.intervalMs = Math.max(50, interval.toMillis());
    }

    public void acquire() {
        try {
            sem.acquire();
            long now = System.currentTimeMillis();
            long wait = lastTime + intervalMs - now;
            if (wait > 0) Thread.sleep(wait);
            lastTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            sem.release();
        }
    }
}
