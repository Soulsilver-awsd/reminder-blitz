package com.paulo.reminder.service;

import com.paulo.reminder.entity.CalendarSource;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SourceSyncService {

    @Inject
    TaskSyncService taskSyncService;

    public CalendarSource createSource(String name, String url) {
        CalendarSource source = new CalendarSource();
        source.name = name;
        source.url = url;
        return source;
    }

    public CalendarSource addSource(String name, String url) {
        return addSource(createSource(name, url));
    }

    public CalendarSource addSource(CalendarSource source) {
        if (source.name == null || source.name.isBlank()) throw new IllegalArgumentException("Nombre obligatorio");
        if (source.url == null || source.url.isBlank()) throw new IllegalArgumentException("Url vacía");

        CalendarSource finalSource = saveOrGetExisting(source);

        try {
            taskSyncService.syncCalendar(finalSource);
        } catch (Exception e) {
            Log.error("Error sync", e);
        }

        return finalSource;
    }

    @Transactional
    public CalendarSource saveOrGetExisting(CalendarSource source) {
        CalendarSource existing = CalendarSource.find("url", source.url).firstResult();

        if (existing != null) {
            existing.name = source.name;
            return existing;
        } else {
            source.id = null;
            source.persist();
            return source;
        }
    }
}