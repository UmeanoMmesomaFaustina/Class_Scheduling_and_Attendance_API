package com.schedule.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Data
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;
    private String instructor;
    private String room;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}