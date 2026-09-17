package com.schedule.api.controller;

import com.schedule.api.dto.ScheduleRequest;
import com.schedule.api.dto.ScheduleResponse;
import com.schedule.api.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * POST /api/schedules
     * Create a new schedule (returns 409 on clash, 422 on invalid time)
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleService.createSchedule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }

    /**
     * DELETE /api/schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/schedules?day=Monday
     * GET /api/schedules?instructorId=1
     * GET /api/schedules?roomId=1
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @RequestParam(required = false) String day,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) Long roomId) {

        if (day != null && !day.isBlank()) {
            return ResponseEntity.ok(scheduleService.getSchedulesByDay(day));
        }
        if (instructorId != null) {
            return ResponseEntity.ok(scheduleService.getSchedulesByInstructor(instructorId));
        }
        if (roomId != null) {
            return ResponseEntity.ok(scheduleService.getSchedulesByRoom(roomId));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * GET /api/schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    /**
     * GET /api/schedules/free-slots?day=Monday&roomId=1&duration=60
     */
    @GetMapping("/free-slots")
    public ResponseEntity<List<String>> getFreeSlots(
            @RequestParam String day,
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "60") int duration) {
        return ResponseEntity.ok(scheduleService.findFreeSlots(day, roomId, duration));
    }
}