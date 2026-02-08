package com.paulo.reminder.integration;

import com.paulo.reminder.dto.TaskDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@QuarkusTest
class MoodleClientTest {
    @Inject
    MoodleClient client;

    @Test
    void testDownloadLocalCalendar(){
        String ruta = "D:/Downloads/icalexport.ics";
        String fakeUrl = new File(ruta).toURI().toString();

        System.out.println("Probando con URL simulada: " + fakeUrl);


        List<TaskDTO> tasks = client.downloadCalendar(fakeUrl);


        Assertions.assertNotNull(tasks, "La lista no debería ser nula");
        Assertions.assertEquals(5, tasks.size(), "Deberían haber 5 tareas");

        TaskDTO primeraTarea = tasks.get(0);
        System.out.println("✅ Tarea encontrada: " + primeraTarea.title());
        System.out.println("✅ Fecha límite: " + primeraTarea.deadline());

        Assertions.assertEquals("Taller_prototipado_Interactivo2 is due", primeraTarea.title());
    }
}