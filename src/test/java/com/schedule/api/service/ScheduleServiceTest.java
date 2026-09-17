package com.schedule.api.service;

import com.schedule.api.dto.ScheduleRequest;
import com.schedule.api.dto.ScheduleResponse;
import com.schedule.api.entity.Course;
import com.schedule.api.entity.Instructor;
import com.schedule.api.entity.Room;
import com.schedule.api.exception.ConflictException;
import com.schedule.api.exception.InvalidDataException;
import com.schedule.api.repository.CourseRepository;
import com.schedule.api.repository.InstructorRepository;
import com.schedule.api.repository.RoomRepository;
import com.schedule.api.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired private ScheduleService scheduleService;
    @Autowired private InstructorRepository instructorRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ScheduleRepository scheduleRepository;

    private Instructor instructor;
    private Room room;
    private Course course;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        instructorRepository.deleteAll();
        roomRepository.deleteAll();
        courseRepository.deleteAll();

        instructor = new Instructor();
        instructor.setName("Test Instructor");
        instructor.setEmail("test@test.edu");
        instructor.setDepartment("CS");
        instructor = instructorRepository.save(instructor);

        room = new Room();
        room.setName("Test Room");
        room.setCapacity(30);
        room.setBuilding("Test Building");
        room = roomRepository.save(room);

        course = new Course();
        course.setCode("TEST101");
        course.setName("Test Course");
        course.setCredits(3);
        course = courseRepository.save(course);
    }

    @Test
    void shouldCreateScheduleSuccessfully() {
        ScheduleRequest req = buildRequest("Monday", "09:00", "10:30");
        ScheduleResponse res = scheduleService.createSchedule(req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isNotNull();
        assertThat(res.getDayOfWeek()).isEqualTo("Monday");
        assertThat(res.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(res.getEndTime()).isEqualTo(LocalTime.of(10, 30));
    }

    @Test
    void shouldRejectInstructorOverlap() {
        scheduleService.createSchedule(buildRequest("Monday", "09:00", "10:30"));

               ScheduleRequest overlapping = buildRequest("Monday", "09:30", "11:00");

        assertThatThrownBy(() -> scheduleService.createSchedule(overlapping))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already has a class scheduled");
    }

    @Test
    void shouldRejectRoomOverlap() {
        scheduleService.createSchedule(buildRequest("Monday", "09:00", "10:30"));

        Instructor another = new Instructor();
        another.setName("Another");
        another.setEmail("another@test.edu");
        another.setDepartment("Math");
        another = instructorRepository.save(another);

        ScheduleRequest req = new ScheduleRequest();
        req.setCourseId(course.getId());
        req.setInstructorId(another.getId());
        req.setRoomId(room.getId());
        req.setDayOfWeek("Monday");
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(11, 30));

        assertThatThrownBy(() -> scheduleService.createSchedule(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        ScheduleRequest req = buildRequest("Monday", "11:00", "09:00");

        assertThatThrownBy(() -> scheduleService.createSchedule(req))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    void shouldAllowAdjacentSchedules() {
        scheduleService.createSchedule(buildRequest("Monday", "09:00", "10:30"));
        ScheduleResponse res = scheduleService.createSchedule(buildRequest("Monday", "10:30", "12:00"));

        assertThat(res.getId()).isNotNull();
    }

    @Test
    void shouldAllowSameTimeOnDifferentDays() {
        scheduleService.createSchedule(buildRequest("Monday", "09:00", "10:30"));
        ScheduleResponse res = scheduleService.createSchedule(buildRequest("Tuesday", "09:00", "10:30"));

        assertThat(res.getId()).isNotNull();
    }

    @Test
    void shouldGetSchedulesByDay() {
        scheduleService.createSchedule(buildRequest("Monday", "09:00", "10:30"));
        scheduleService.createSchedule(buildRequest("Tuesday", "09:00", "10:30"));

        assertThat(scheduleService.getSchedulesByDay("Monday")).hasSize(1);
        assertThat(scheduleService.getSchedulesByDay("Tuesday")).hasSize(1);
        assertThat(scheduleService.getSchedulesByDay("Wednesday")).isEmpty();
    }

    private ScheduleRequest buildRequest(String day, String start, String end) {
        ScheduleRequest req = new ScheduleRequest();
        req.setCourseId(course.getId());
        req.setInstructorId(instructor.getId());
        req.setRoomId(room.getId());
        req.setDayOfWeek(day);
        req.setStartTime(LocalTime.parse(start));
        req.setEndTime(LocalTime.parse(end));
        req.setSemester("Fall 2026");
        req.setMaxStudents(30);
        return req;
    }
}