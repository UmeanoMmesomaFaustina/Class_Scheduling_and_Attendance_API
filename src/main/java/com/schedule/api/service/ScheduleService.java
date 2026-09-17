package com.schedule.api.service;

import com.schedule.api.dto.ScheduleRequest;
import com.schedule.api.dto.ScheduleResponse;
import com.schedule.api.entity.Course;
import com.schedule.api.entity.Instructor;
import com.schedule.api.entity.Room;
import com.schedule.api.entity.Schedule;
import com.schedule.api.exception.ConflictException;
import com.schedule.api.exception.InvalidDataException;
import com.schedule.api.exception.NotFoundException;
import com.schedule.api.repository.CourseRepository;
import com.schedule.api.repository.InstructorRepository;
import com.schedule.api.repository.RoomRepository;
import com.schedule.api.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        log.info("Creating schedule: {}", request);

        validateTimeRange(request.getStartTime(), request.getEndTime());

        Instructor instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new NotFoundException("Instructor not found with ID: " + request.getInstructorId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("Room not found with ID: " + request.getRoomId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new NotFoundException("Course not found with ID: " + request.getCourseId()));

        checkForClashes(request, instructor, room);

        Schedule schedule = new Schedule();
        schedule.setCourse(course);
        schedule.setInstructor(instructor);
        schedule.setRoom(room);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSemester(request.getSemester());
        schedule.setMaxStudents(request.getMaxStudents());

        Schedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created successfully with ID: {}", saved.getId());

        return convertToResponse(saved);
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "daySchedules", key = "#dayOfWeek")
    public List<ScheduleResponse> getSchedulesByDay(String dayOfWeek) {
        log.info("Fetching schedules for day: {}", dayOfWeek);
        List<Schedule> schedules = scheduleRepository.findByDayOfWeek(dayOfWeek);
        return schedules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "instructorSchedules", key = "#instructorId")
    public List<ScheduleResponse> getSchedulesByInstructor(Long instructorId) {
        log.info("Fetching schedules for instructor ID: {}", instructorId);
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new NotFoundException("Instructor not found"));

        List<Schedule> schedules = scheduleRepository.findByInstructor(instructor);
        return schedules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "roomSchedules", key = "#roomId")
    public List<ScheduleResponse> getSchedulesByRoom(Long roomId) {
        log.info("Fetching schedules for room ID: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));


        List<Schedule> schedules = scheduleRepository.findByRoom(room);
        return schedules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + id));
        return convertToResponse(schedule);
    }

    @Transactional
    @CacheEvict(value = {"daySchedules", "instructorSchedules", "roomSchedules"}, allEntries = true)
    public ScheduleResponse updateSchedule(Long id, ScheduleRequest request) {
        log.info("Updating schedule ID: {}", id);

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + id));

        validateTimeRange(request.getStartTime(), request.getEndTime());
        Instructor instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new NotFoundException("Instructor not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new NotFoundException("Course not found"));

        checkForClashes(request, instructor, room, id);

        existingSchedule.setCourse(course);
        existingSchedule.setInstructor(instructor);
        existingSchedule.setRoom(room);
        existingSchedule.setDayOfWeek(request.getDayOfWeek());
        existingSchedule.setStartTime(request.getStartTime());
        existingSchedule.setEndTime(request.getEndTime());
        existingSchedule.setSemester(request.getSemester());
        existingSchedule.setMaxStudents(request.getMaxStudents());

        Schedule updated = scheduleRepository.save(existingSchedule);
        log.info("Schedule updated successfully: {}", id);

        return convertToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = {"daySchedules", "instructorSchedules", "roomSchedules"}, allEntries = true)
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule ID: {}", id);
        if (!scheduleRepository.existsById(id)) {
            throw new NotFoundException("Schedule not found with ID: " + id);
        }
        scheduleRepository.deleteById(id);
        log.info("Schedule deleted successfully: {}", id);
    }
    @Transactional(readOnly = true)
    public List<String> findFreeSlots(String dayOfWeek, Long roomId, int durationMinutes) {
        log.info("Finding free slots for day: {}, room: {}, duration: {}", dayOfWeek, roomId, durationMinutes);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        List<Schedule> existingSchedules = scheduleRepository.findByRoomAndDay(room, dayOfWeek);

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(22, 0);

        List<String> freeSlots = new java.util.ArrayList<>();
        LocalTime current = start;

        for (Schedule schedule : existingSchedules) {
            if (current.plusMinutes(durationMinutes).isAfter(schedule.getStartTime())) {
                current = schedule.getEndTime();
                continue;
            }
            freeSlots.add(current + " - " + current.plusMinutes(durationMinutes));
            current = schedule.getEndTime();
        }

        if (current.plusMinutes(durationMinutes).isBefore(end) || current.plusMinutes(durationMinutes).equals(end)) {
            freeSlots.add(current + " - " + current.plusMinutes(durationMinutes));
        }

        return freeSlots;
    }

        private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new InvalidDataException("Start time and end time are required");
        }
        if (start.isAfter(end) || start.equals(end)) {
            throw new InvalidDataException("Start time must be before end time");
        }
        if (start.isBefore(LocalTime.of(6, 0)) || end.isAfter(LocalTime.of(23, 59))) {
            throw new InvalidDataException("Schedule must be between 6:00 AM and 11:59 PM");
        }
    }

    private void checkForClashes(ScheduleRequest request, Instructor instructor, Room room) {
        checkForClashes(request, instructor, room, null);
    }

    private void checkForClashes(ScheduleRequest request, Instructor instructor, Room room, Long excludeId) {

        List<Schedule> instructorSchedules = scheduleRepository.findOverlappingSchedulesForInstructor(
                request.getDayOfWeek(),
                instructor,
                request.getStartTime(),
                request.getEndTime()
        );

        if (!instructorSchedules.isEmpty()) {
                     if (excludeId != null) {
                instructorSchedules = instructorSchedules.stream()
                        .filter(s -> !s.getId().equals(excludeId))
                        .collect(Collectors.toList());
            }
            if (!instructorSchedules.isEmpty()) {
                throw new ConflictException(
                        "Instructor '" + instructor.getName() + "' already has a class scheduled at " +
                                request.getStartTime() + " on " + request.getDayOfWeek() +
                                " (Course: " + instructorSchedules.get(0).getCourse().getName() + ")"
                );
            }
        }
        List<Schedule> roomSchedules = scheduleRepository.findOverlappingSchedulesForRoom(
                request.getDayOfWeek(),
                room,
                request.getStartTime(),
                request.getEndTime()
        );

        if (!roomSchedules.isEmpty()) {
            if (excludeId != null) {
                roomSchedules = roomSchedules.stream()
                        .filter(s -> !s.getId().equals(excludeId))
                        .collect(Collectors.toList());
            }
            if (!roomSchedules.isEmpty()) {
                throw new ConflictException(
                        "Room '" + room.getName() + "' is already booked at " +
                                request.getStartTime() + " on " + request.getDayOfWeek() +
                                " (Course: " + roomSchedules.get(0).getCourse().getName() + ")"
                );
            }
        }
    }
        private ScheduleResponse convertToResponse(Schedule schedule) {
        String courseCode = schedule.getCourse().getCode();
        String courseName = schedule.getCourse().getName();
        String instructorName = schedule.getInstructor().getName();
        String roomName = schedule.getRoom().getName();

        return new ScheduleResponse(
                schedule.getId(),
                courseCode,
                courseName,
                instructorName,
                roomName,
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSemester(),
                schedule.getMaxStudents()
        );
    }
}