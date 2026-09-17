package com.schedule.api.service;

import com.schedule.api.dto.AttendanceRequest;
import com.schedule.api.dto.AttendanceResponse;
import com.schedule.api.entity.Attendance;
import com.schedule.api.entity.Schedule;
import com.schedule.api.exception.ConflictException;
import com.schedule.api.exception.NotFoundException;
import com.schedule.api.repository.AttendanceRepository;
import com.schedule.api.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        log.info("Marking attendance for student: {} on date: {}", request.getStudentId(), request.getDate());

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + request.getScheduleId()));
        attendanceRepository.findByScheduleAndStudentIdAndDate(
                schedule, request.getStudentId(), request.getDate()
        ).ifPresent(a -> {
            throw new ConflictException(
                    "Attendance already marked for student " + request.getStudentId() +
                            " on " + request.getDate()
            );
        });

        Attendance attendance = new Attendance();
        attendance.setSchedule(schedule);
        attendance.setStudentId(request.getStudentId());
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus() != null ? request.getStatus() : "PRESENT");
        attendance.setMarkedBy(request.getMarkedBy());

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance marked successfully with ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Transactional
    public void undoAttendance(Long attendanceId) {
        log.info("Undoing attendance ID: {}", attendanceId);
        if (!attendanceRepository.existsById(attendanceId)) {
            throw new NotFoundException("Attendance record not found with ID: " + attendanceId);
        }
        attendanceRepository.deleteById(attendanceId);
        log.info("Attendance undone successfully: {}", attendanceId);
    }
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceBySchedule(Long scheduleId) {
        log.info("Fetching attendance for schedule ID: {}", scheduleId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        List<Attendance> attendanceList = attendanceRepository.findBySchedule(schedule);
        return attendanceList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public AttendanceSummary getAttendanceSummary(Long scheduleId) {
        log.info("Getting attendance summary for schedule ID: {}", scheduleId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        List<Attendance> allAttendance = attendanceRepository.findBySchedule(schedule);

        long present = allAttendance.stream()
                .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()))
                .count();
        long absent = allAttendance.stream()
                .filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus()))
                .count();

        return new AttendanceSummary(
                scheduleId,
                schedule.getCourse().getName(),
                allAttendance.size(),
                present,
                absent
        );
    }
    @Transactional
    public AttendanceResponse updateAttendanceStatus(Long attendanceId, String newStatus) {
        log.info("Updating attendance ID: {} to status: {}", attendanceId, newStatus);

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NotFoundException("Attendance record not found"));

        if (!"PRESENT".equalsIgnoreCase(newStatus) && !"ABSENT".equalsIgnoreCase(newStatus)) {
            throw new IllegalArgumentException("Status must be 'PRESENT' or 'ABSENT'");
        }

        attendance.setStatus(newStatus.toUpperCase());
        Attendance updated = attendanceRepository.save(attendance);
        return convertToResponse(updated);
    }

    private AttendanceResponse convertToResponse(Attendance attendance) {
        Schedule schedule = attendance.getSchedule();
        Long scheduleId = schedule.getId();
        String courseName = schedule.getCourse().getName();

        return new AttendanceResponse(
                attendance.getId(),
                scheduleId,
                courseName,
                attendance.getStudentId(),
                attendance.getDate(),
                attendance.getStatus(),
                attendance.getMarkedBy()
        );
    }
    public static class AttendanceSummary {
        private final Long scheduleId;
        private final String courseName;
        private final long totalStudents;
        private final long presentCount;
        private final long absentCount;

        public AttendanceSummary(Long scheduleId, String courseName, long totalStudents,
                                 long presentCount, long absentCount) {
            this.scheduleId = scheduleId;
            this.courseName = courseName;
            this.totalStudents = totalStudents;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }
        public Long getScheduleId() { return scheduleId; }
        public String getCourseName() { return courseName; }
        public long getTotalStudents() { return totalStudents; }
        public long getPresentCount() { return presentCount; }
        public long getAbsentCount() { return absentCount; }
    }
}