package com.paulo.reminder.api;

import com.paulo.reminder.entity.CalendarSource;
import com.paulo.reminder.service.SourceSyncService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;


@Path("/api/source")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CalendarSourceResource {

    @Inject
    SourceSyncService sourceSyncService;

    @POST
    public Response postSource(CalendarSource source) {
        Log.info("📨 Recibiendo petición POST para guardar fuente...");

        try {
            CalendarSource savedSource = sourceSyncService.addSource(source);
            return Response.status(201).entity(savedSource).build();

        } catch (IllegalArgumentException e) {
            Log.warn("Validación fallida: " + e.getMessage());

            return Response.status(400)
                    .entity(Map.of("error", e.getMessage()))
                    .build();

        } catch (Exception e) {
            Log.error("Error CRÍTICO guardando la fuente", e);

            return Response.serverError()
                    .entity(Map.of(
                            "error", "Error interno del servidor",
                            "detalle", "Revisa los logs del servidor para más info"
                    ))
                    .build();
        }
    }
}
