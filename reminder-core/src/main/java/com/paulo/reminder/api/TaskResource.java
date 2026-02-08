package com.paulo.reminder.api;

import com.paulo.reminder.entity.Task;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource {
    @GET
    public List<Task> getAll(){
        return Task.list("order by deadline asc");
    }
}
