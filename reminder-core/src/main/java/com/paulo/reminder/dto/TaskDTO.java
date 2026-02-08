package com.paulo.reminder.dto;

import java.time.LocalDateTime;

public record TaskDTO(Long id,
                      String title,
                      String description,
                      LocalDateTime deadline,
                      boolean isDone,
                      String sourceName,
                      String uId){
}