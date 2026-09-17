package com.schedule.api.service;

import com.schedule.api.dto.AttendanceRequest;
import com.schedule.api.dto.AttendanceResponse;
import com.schedule.api.dto.ScheduleRequest;
import com.schedule.api.entity.Course;
import com.schedule.api.entity.Instructor;
import com.schedule.api.entity.Room;
import com.schedule.api.exception.ConflictException;
import com.schedule.api.repository.AttendanceRepository;
import com.schedule.api.repository.CourseRepository;
import com.schedule.api.repository.InstructorRepository;
import com.schedule.api.repository.RoomRepository;
import com.schedule.api.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AttendanceServiceTest {

    @Autowired private AttendanceService attendanceService;
    @Autowired private ScheduleService scheduleService;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private InstructorRepository instructorRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private CourseRepository courseRepository;

    private Long scheduleId;

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        scheduleRepository.deleteAll();
        instructorRepository.deleteAll();
        roomRepository.deleteAll();
        courseRepository.deleteAll();

        Instructor inst = new Instructor();
        inst.setName("Test"); inst.setEmail("t@t.edu"); inst.setDepartment("CS");
        inst = instructorRepository.save(inst);

        Room room = new Room();
        room.setName("R1"); room.setCapacity(20); room.setBuilding("B1");
        room = roomRepository.save(room);

        Course course = new Course();
        course.setCode("T1"); course.setName("Test"); course.setCredits(3);
        course = courseRepository.save(course);

        ScheduleRequest req = new ScheduleRequest();
        req.setCourseId(course.getId());
        req.setInstructorId(inst.getId());
        req.setRoomId(room.getId());
        req.setDayOfWeek("Monday");
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(10, 0));
        scheduleId = scheduleService.createSchedule(req).getId();
    }

    @Test
    void shouldMarkAttendanceSuccessfully() {
        AttendanceRequest req = new AttendanceRequest();
        req.setScheduleId(scheduleId);
        req.setStudentId("S001");
        req.setDate(LocalDate.now());
        req.setStatus("PRESENT");

        AttendanceResponse res = attendanceService.markAttendance(req);

        assertThat(res.getId()).isNotNull();
        assertThat(res.getStatus()).isEqualTo("PRESENT");
        assertThat(res.getStudentId()).isEqualTo("S001");
    }

    @Test
    void shouldRejectDuplicateAttendance() {
        AttendanceRequest req = new AttendanceRequest();
        req.setScheduleId(scheduleId);
        req.setStudentId("S001");
        req.setDate(LocalDate.now());
        req.setStatus("PRESENT");

        attendanceService.markAttendance(req);

        assertThatThrownBy(() -> attendanceService.markAttendance(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already marked");
    }

    @Test
    void shouldUndoAttendance() {
        AttendanceRequest req = new AttendanceRequest();
        req.setScheduleId(scheduleId);
        req.setStudentId("S001");
        req.setDate(LocalDate.now());
        req.setStatus("PRESENT");

        AttendanceResponse res = attendanceService.markAttendance(req);
        attendanceService.undoAttendance(res.getId());

        assertThat(attendanceService.getAttendanceBySchedule(scheduleId)).isEmpty();
    }

    @Test
    void shouldCalculateAttendanceSummary() {
        AttendanceRequest p = new AttendanceRequest();
        p.setScheduleId(scheduleId);
        p.setStudentId("S001");
        p.setDate(LocalDate.now());
        p.setStatus("PRESENT");
        attendanceService.markAttendance(p);

        AttendanceRequest a = new AttendanceRequest();
        a.setScheduleId(scheduleId);
        a.setStudentId("S002");
        a.setDate(LocalDate.now());
        a.setStatus("ABSENT");
        attendanceService.markAttendance(a);

        AttendanceService.AttendanceSummary summary =
                attendanceService.getAttendanceSummary(scheduleId);

        assertThat(summary.getTotalStudents()).isEqualTo(2);
        assertThat(summary.getPresentCount()).isEqualTo(1);
        assertThat(summary.getAbsentCount()).isEqualTo(1);
    }
}