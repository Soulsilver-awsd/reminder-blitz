package com.paulo.reminder.dto;

public record TaskDTO(String summary,
                      String deadline,
                      String description,
                      boolean idDone,
                      String sourceName){
}
