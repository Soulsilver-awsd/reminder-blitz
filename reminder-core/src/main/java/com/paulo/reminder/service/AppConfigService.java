package com.paulo.reminder.service;

import com.paulo.reminder.entity.AppConfig;
import com.paulo.reminder.event.ConfigChangedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AppConfigService {

    @Inject
    Event<ConfigChangedEvent> trigger;

    @Transactional
    public AppConfig getOrCreateConfig() {
        AppConfig config = AppConfig.findAll().firstResult();

        if (config == null) {
            config = new AppConfig();
            config.ownerName = "User";
            config.email = "";
            config.interval = 60;
            config.persist();
        }
        return config;
    }

    public AppConfig mapToConfig(String ownerName, String email, int interval) {
        AppConfig tempConfig = new AppConfig();

        tempConfig.ownerName = (ownerName != null && !ownerName.isBlank()) ? ownerName : null;
        tempConfig.email = (email != null && !email.isBlank()) ? email : null;
        tempConfig.interval = (interval >= 5) ? interval : 0;

        return tempConfig;
    }

    @Transactional
    public void updateConfig(AppConfig incomingData) {
        AppConfig dbConfig = getOrCreateConfig();

        if (incomingData.ownerName != null) dbConfig.ownerName = incomingData.ownerName;
        if (incomingData.email != null) dbConfig.email = incomingData.email;
        if (incomingData.interval >= 5) dbConfig.interval = incomingData.interval;

        trigger.fire(new ConfigChangedEvent(dbConfig.interval));
    }

    @Transactional
    public void updateConfig(String ownerName, String email, int interval) {
        AppConfig configObject = mapToConfig(ownerName, email, interval);
        updateConfig(configObject);
    }
}