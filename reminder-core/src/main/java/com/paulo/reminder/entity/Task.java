package com.paulo.reminder.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Task extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String title;
    public String description;
    @Column(unique = true)
    public String uId;
    public LocalDateTime deadline;
    public boolean isDone;
    public String sourceName;
    public Long calendarSourceId;

    public Task() {}
}
