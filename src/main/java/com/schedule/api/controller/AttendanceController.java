package com.schedule.api.controller;

import com.schedule.api.dto.AttendanceRequest;
import com.schedule.api.dto.AttendanceResponse;
import com.schedule.api.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * POST /api/attendance
     */
    @PostMapping
    public ResponseEntity<AttendanceResponse> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.markAttendance(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * DELETE /api/attendance/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> undoAttendance(@PathVariable Long id) {
        attendanceService.undoAttendance(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/attendance/schedule/{scheduleId}
     */
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceBySchedule(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(attendanceService.getAttendanceBySchedule(scheduleId));
    }

    /**
     * GET /api/attendance/summary/{scheduleId}
     */
    @GetMapping("/summary/{scheduleId}")
    public ResponseEntity<AttendanceService.AttendanceSummary> getSummary(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(attendanceService.getAttendanceSummary(scheduleId));
    }

    /**
     * PATCH /api/attendance/{id}/status?newStatus=ABSENT
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AttendanceResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String newStatus) {
        return ResponseEntity.ok(attendanceService.updateAttendanceStatus(id, newStatus));
    }
}