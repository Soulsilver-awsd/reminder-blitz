package com.paulo.reminder.event;

public class ConfigChangedEvent {
    public final int newInterval;

    public ConfigChangedEvent(int newInterval) {
        this.newInterval = newInterval;
    }
}