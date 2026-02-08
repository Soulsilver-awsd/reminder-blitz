package com.paulo.reminder.service;

import com.paulo.reminder.dto.TaskDTO;
import com.paulo.reminder.entity.CalendarSource;
import com.paulo.reminder.entity.Task;
import com.paulo.reminder.integration.MoodleClient;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class TaskSyncService {

    //TODO: v2.0 Aplicar Repository Pattern

    @Inject
    MoodleClient moodleClient;

    public void syncAll(){
        Log.info("Iniciando sincronización de todas las fuentes");

        List<CalendarSource> allSources = getAllSources();

        if(allSources.isEmpty()){
            Log.warn("No hay fuentes para sincronizar");
            return;
        }

        for(CalendarSource source : allSources){
            try{
                syncCalendar(source);
            }catch(Exception e){
                Log.error("Error sincronizando: " + source.name, e);
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
        java.util.Map<String, Task> taskMap = Task.list("calendarSourceId", source.id)
                .stream()
                .map(t -> (Task) t)
                .collect(java.util.stream.Collectors.toMap(t -> t.uId, t -> t));

        int newTasks = 0;
        int taskUpdated = 0;

        for (TaskDTO dto : tasks) {
            Task existingTask = taskMap.get(dto.uId());

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

                newTasks++;
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
                if (changed) taskUpdated++;
            }
        }
        Log.infof("%s -> Nuevas: %d, Actualizadas: %d", source.name, newTasks, taskUpdated);
    }

}