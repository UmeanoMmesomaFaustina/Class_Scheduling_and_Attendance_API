package com.schedule.api.repository;

import com.schedule.api.entity.Attendance;
import com.schedule.api.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findBySchedule(Schedule schedule);

    List<Attendance> findByScheduleAndDate(Schedule schedule, LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.schedule = :schedule AND a.date = :date")
    List<Attendance> findAttendanceForDate(@Param("schedule") Schedule schedule,
                                           @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.schedule = :schedule AND a.status = 'PRESENT'")
    long countPresent(@Param("schedule") Schedule schedule);

    Optional<Attendance> findByScheduleAndStudentIdAndDate(
            Schedule schedule, String studentId, LocalDate date);
}