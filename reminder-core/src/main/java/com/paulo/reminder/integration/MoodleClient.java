package com.paulo.reminder.integration;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import com.paulo.reminder.dto.TaskDTO;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class MoodleClient
{
    public List<TaskDTO> downloadCalendar(String url) {

        if (url == null || url.isBlank()) {
            Log.info("La url para la descarga del calendario está vacía");
            return Collections.emptyList();
        }
        Log.info("Iniciando descarga");
        ICalendar calendar = null;
        try (InputStream in = new URL(url).openStream()) {
            calendar = Biweekly.parse(in).first();
            if (calendar == null) {
                Log.info("Error en el parseo");
                return Collections.emptyList();
            }
            return calendar.getEvents().stream()
                    .map(this::eventToDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Log.error("Error en la descarga: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    private TaskDTO eventToDTO(VEvent event){
        if (event.getUid() == null) return null;
        String id = event.getUid().getValue();
        String title = (event.getSummary() != null)? event.getSummary().getValue() : "Sin Titulo";
        String description = (event.getDescription() != null)? event.getDescription().getValue() : "Sin Descripcion";
        LocalDateTime deadline = null;
        if (event.getDateStart() != null) {
            deadline = event.getDateStart().getValue().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } else {
            return null;
        }
        return new TaskDTO(null, title, description, deadline, false, null, id);
    }
}
