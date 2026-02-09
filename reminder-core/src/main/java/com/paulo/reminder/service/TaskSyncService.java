package com.paulo.reminder.service;

import com.paulo.reminder.dto.TaskDTO;
import com.paulo.reminder.entity.AppConfig;
import com.paulo.reminder.entity.CalendarSource;
import com.paulo.reminder.entity.Task;
import com.paulo.reminder.event.ConfigChangedEvent;
import com.paulo.reminder.integration.MoodleClient;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskSyncService {

    @Inject
    MoodleClient moodleClient;
    @Inject
    Scheduler scheduler;
    @Inject
    AppConfigService appConfigService;
    @Inject
    DesktopNotificationService notificationService;

    private static final String JOB_NAME = "moodle-sync-job";

    @Scheduled(every = "1000h")
    void keepAlive() {
    }

    void onStart(@Observes StartupEvent ev) {
        Log.info("Arrancando Sincronizador Dinámico...");
        AppConfig config = appConfigService.getOrCreateConfig();
        scheduleJob(config.interval);
    }

    void onConfigChange(@Observes ConfigChangedEvent event) {
        scheduleJob(event.newInterval);
        Log.info("Configuración cambiada. Forzando sincronización en hilo virtual...");
        Thread.ofVirtual().start(this::syncAll);
    }

    private void scheduleJob(int intervalMinutes) {
        if (scheduler.getScheduledJob(JOB_NAME) != null) {
            scheduler.unscheduleJob(JOB_NAME);
        }
        if (intervalMinutes <= 0) return;

        scheduler.newJob(JOB_NAME)
                .setInterval(intervalMinutes + "m")
                .setTask(executionContext -> syncAll())
                .schedule();
        Log.info("Sincronización programada cada " + intervalMinutes + " minutos.");
    }

    public void syncAll() {
        Log.info("Iniciando sincronización de todas las fuentes");

        List<CalendarSource> allSources = getAllSources();

        if (allSources.isEmpty()) {
            Log.warn("No hay fuentes para sincronizar");
            return;
        }

        for (CalendarSource source : allSources) {
            try {
                syncCalendar(source);
            } catch (Exception e) {
                Log.error("Error sincronizando: " + source.name, e);
                notificationService.showError("Error de Sync", "Falló al conectar con: " + source.name);
            }
        }
        Log.info("Sincronización masiva terminada");
    }

    @Transactional
    public List<CalendarSource> getAllSources() {
        return CalendarSource.listAll();
    }

    public void syncCalendar(CalendarSource source) {
        Log.infof(" Descargando: %s", source.name);

        List<TaskDTO> downloadedTasks = moodleClient.downloadCalendar(source.url);

        if (downloadedTasks.isEmpty()) {
            Log.warnf(" La fuente '%s' no devolvió tareas.", source.name);
            return;
        }

        saveTasksToDatabase(downloadedTasks, source);
    }

    @Transactional
    public void saveTasksToDatabase(List<TaskDTO> tasks, CalendarSource source) {
        Log.infof("Procesando %d tareas para: %s", tasks.size(), source.name);

        Map<String, Task> taskMap = Task.list("calendarSourceId", source.id)
                .stream()
                .map(t -> (Task) t)
                .collect(Collectors.toMap(t -> t.uId, t -> t));

        int newTasksCount = 0;
        int taskUpdatedCount = 0;

        for (TaskDTO dto : tasks) {
            Task existingTask = taskMap.get(dto.uId());
            Task taskToNotify;

            if (existingTask == null) {
                Task newTask = new Task();
                newTask.title = dto.title();
                newTask.description = dto.description();
                newTask.uId = dto.uId();
                newTask.deadline = dto.deadline();
                newTask.isDone = false;
                newTask.sourceName = source.name;
                newTask.calendarSourceId = source.id;

                newTask.persist();
                taskMap.put(newTask.uId, newTask);

                taskToNotify = newTask;
                newTasksCount++;
            } else {
                boolean changed = false;
                if (!existingTask.deadline.isEqual(dto.deadline())) {
                    existingTask.deadline = dto.deadline();
                    changed = true;
                }
                if (!existingTask.title.equals(dto.title())) {
                    existingTask.title = dto.title();
                    changed = true;
                }
                if (changed) taskUpdatedCount++;

                taskToNotify = existingTask;
            }

            notificationService.blitzNewTask(taskToNotify);
        }
        Log.infof("%s -> Nuevas: %d, Actualizadas: %d", source.name, newTasksCount, taskUpdatedCount);
    }
}