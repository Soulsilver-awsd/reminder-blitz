package com.paulo.reminder.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class CalendarSource extends PanacheEntity {
    public String name;
    @Column(length = 2048)
    public String url;
    public CalendarSource(){}
}
