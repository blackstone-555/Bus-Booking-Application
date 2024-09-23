package com.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;


@Table(name = "schedules")
@Data
@NoArgsConstructor
public class Schedule {

    @Id
    int scheduleId;

    String source;

    String destination;

    LocalDate date;
    
}
