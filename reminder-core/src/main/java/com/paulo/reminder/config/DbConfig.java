package com.paulo.reminder.config;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@ApplicationScoped
public class DbConfig {

    @Inject
    DataSource dataSource;

    void onStart(@Observes StartupEvent ev) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA journal_mode = WAL;");

            stmt.execute("PRAGMA busy_timeout = 30000;");

            Log.info("BASE DE DATOS: Modo WAL activado y Timeout de 30s configurado.");
        } catch (Exception e) {
            Log.error("Error configurando SQLite", e);
        }
    }
}