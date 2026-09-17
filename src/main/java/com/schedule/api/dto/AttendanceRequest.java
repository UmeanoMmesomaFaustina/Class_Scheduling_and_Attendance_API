package com.schedule.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {
    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String status;
    private String markedBy;
}