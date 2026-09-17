package com.schedule.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long scheduleId;
    private String courseName;
    private String studentId;
    private LocalDate date;
    private String status;
    private String markedBy;
}