package com.paulo.reminder.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Task extends PanacheEntity {
    public String title;
    public String description;
    @Column(unique = true)
    public String externalUid;
    public LocalDateTime deadline;
    public boolean isDone;
    public String sourceName;
    public Long calendarSourceId;

    public Task() {}
}
