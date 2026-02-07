package com.paulo.reminder.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class AppConfig extends PanacheEntity {
    public String ownerName;
    public String email;


    public AppConfig(){}
}
