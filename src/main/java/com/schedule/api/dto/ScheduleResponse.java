package com.schedule.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private String courseCode;
    private String courseName;
    private String instructorName;
    private String roomName;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String semester;
    private Integer maxStudents;
}