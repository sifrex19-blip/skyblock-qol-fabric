package com.skyblockqol;

import java.util.ArrayList;
import java.util.List;

public class TimerBoard {
    public static class Timer {
        public final String name;
        public long endAt;
        public Timer(String name, long endAt) { this.name = name; this.endAt = endAt; }
        public long remaining() { return endAt - System.currentTimeMillis(); }
    }
    private final List<Timer> timers = new ArrayList<>();

    public void add(String name, long durationMs) {
        timers.add(new Timer(name, System.currentTimeMillis() + durationMs));
    }
    public void tick() {
        timers.removeIf(t -> t.remaining() <= 0);
    }
    /** Removes every running timer (console "CLEAR" button). */
    public void clear() {
        timers.clear();
    }
    public List<Timer> all() { return timers; }
}