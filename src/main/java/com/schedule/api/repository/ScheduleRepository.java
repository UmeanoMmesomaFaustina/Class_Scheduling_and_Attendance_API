package com.schedule.api.repository;

import com.schedule.api.entity.Schedule;
import com.schedule.api.entity.Instructor;
import com.schedule.api.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s WHERE s.instructor = :instructor AND s.dayOfWeek = :day")
    List<Schedule> findByInstructorAndDay(@Param("instructor") Instructor instructor,
                                          @Param("day") String day);

    @Query("SELECT s FROM Schedule s WHERE s.room = :room AND s.dayOfWeek = :day")
    List<Schedule> findByRoomAndDay(@Param("room") Room room,
                                    @Param("day") String day);

    List<Schedule> findByDayOfWeek(String dayOfWeek);
    List<Schedule> findByInstructor(Instructor instructor);
    List<Schedule> findByRoom(Room room);
    @Query("SELECT s FROM Schedule s WHERE s.dayOfWeek = :day " +
            "AND s.instructor = :instructor " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findOverlappingSchedulesForInstructor(
            @Param("day") String day,
            @Param("instructor") Instructor instructor,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT s FROM Schedule s WHERE s.dayOfWeek = :day " +
            "AND s.room = :room " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findOverlappingSchedulesForRoom(
            @Param("day") String day,
            @Param("room") Room room,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}