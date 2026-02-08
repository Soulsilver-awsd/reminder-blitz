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
        List<CalendarSource> allSources = CalendarSource.listAll();

        if(allSources.isEmpty()){
            Log.warn("No hay fuentes para sincronizar");
            return;
        }
        for(CalendarSource source : allSources){
            try{
                syncCalendar(source);

            }catch(Exception e){
                Log.error("Error en: " + source.name, e);
            }
        }
        Log.info("Sincronización terminada");
    }
    @Transactional
    public void syncCalendar(CalendarSource calendar){
        List<TaskDTO> downloadedTasks= moodleClient.downloadCalendar(calendar.url);
        int newTasks = 0, taskUpdated = 0;
        for(TaskDTO dto : downloadedTasks){
            if (taskInDatabase(dto, calendar)) {
                taskUpdated++;
            } else {
                newTasks++;
            }
        }
        Log.info(" Tareas actualizadas: " + taskUpdated + " Tareas agregadas: " + newTasks);
    }


    private boolean taskInDatabase(TaskDTO task, CalendarSource source){
        Task existingTask = Task.find("uId", task.uId()).firstResult();
        if(existingTask == null){
            Task newTask = new Task();
            newTask.title = task.title();
            newTask.description = task.description();
            newTask.uId = task.uId();
            newTask.deadline = task.deadline();
            newTask.isDone = false;
            newTask.sourceName = source.name;
            newTask.calendarSourceId = source.id;
            newTask.persist();
            return false;
        }else{

            if (!existingTask.deadline.isEqual(task.deadline())) {
                existingTask.deadline = task.deadline();
                Log.info(" Fecha actualizada para: " + task.title());
            }

            if (!existingTask.title.equals(task.title())) {
                Log.info(existingTask.title +" Renombrado a: " + task.title());
                existingTask.title = task.title();
            }

            return true;
        }


    }
}
