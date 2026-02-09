package com.paulo.reminder.service;

import com.paulo.reminder.entity.Task;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class DesktopNotificationService {

    private TrayIcon trayIcon;
    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        } else {
            Log.error("Error: TrayIcon no inicializado ");
        }
    }

    public void showInfo(String title, String message) {
        showNotification(title, message, TrayIcon.MessageType.INFO);
    }

    public void showError(String title, String message) {
        showNotification(title, message, TrayIcon.MessageType.ERROR);
    }


    public void blitzNewTask(Task task) {
        if (trayIcon == null) return;
        Log.info("INTENTANDO ENVIAR NOTIFICACIÓN A WINDOWS...");
        String title = "Blitz: " + task.title;
        String tiempoRestante = formatTimeRemaining(task.deadline);

        String mensaje = String.format("%s\nOrigen: %s", tiempoRestante, task.sourceName);

        trayIcon.displayMessage(title, mensaje, TrayIcon.MessageType.INFO);
    }

    private String formatTimeRemaining(LocalDateTime deadline) {
        if (deadline == null) return "Sin fecha límite";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, deadline);

        if (duration.isNegative()) {
            long horasVencidas = Math.abs(duration.toHours());
            return "Venció hace " + horasVencidas + "h";
        }

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            return String.format("Vence en: %dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("Vence en: %dh %dm", hours, minutes);
        } else {
            return String.format("Vence en: %dm", minutes);
        }
    }
}