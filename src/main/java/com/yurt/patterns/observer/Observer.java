package com.yurt.patterns.observer;

public interface Observer {
    // Bir olay olduğunda bu metot tetiklenecek
    void update(String message);
}