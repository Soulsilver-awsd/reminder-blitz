package com.paulo.reminder.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Task extends PanacheEntity {
    public String summary;
    @Column(unique = true)
    public String uId;
    public LocalDateTime dtStart;
    public LocalDateTime dtEnd;
    public String description;
    public boolean idDone;
    public String sourceName;
    public Long calendarSourceId;

    public Task() {}
}
