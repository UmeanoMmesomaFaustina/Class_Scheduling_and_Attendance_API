package com.schedule.api.config;

import com.schedule.api.entity.Course;
import com.schedule.api.entity.Instructor;
import com.schedule.api.entity.Room;
import com.schedule.api.repository.CourseRepository;
import com.schedule.api.repository.InstructorRepository;
import com.schedule.api.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
    @Autowired(required = false)
    private CacheManager cacheManager;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final CourseRepository courseRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading sample data...");
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
            log.info(" Cleared all caches");
        }

        if (instructorRepository.count() == 0) {
            Instructor instructor1 = new Instructor();
            instructor1.setName("Dr. Julius Olajire");
            instructor1.setEmail("julius.olajire@veritas.edu");
            instructor1.setDepartment("Software Engineering");
            instructorRepository.save(instructor1);

            Instructor instructor2 = new Instructor();
            instructor2.setName("Mr. Ibukun Adeshina");
            instructor2.setEmail("ibukun.adeshina@veritas.edu");
            instructor2.setDepartment("Software Engineering");
            instructorRepository.save(instructor2);

            log.info("Created sample instructors");
        }

        if (roomRepository.count() == 0) {
            Room room1 = new Room();
            room1.setName("SEN lab");
            room1.setCapacity(150);
            room1.setBuilding(" Software Engineering Building");
            roomRepository.save(room1);

            Room room2 = new Room();
            room2.setName("SEN hall 1");
            room2.setCapacity(300);
            room2.setBuilding(" Software Engineering Building");
            roomRepository.save(room2);

            log.info("Created sample rooms");
        }

        if (courseRepository.count() == 0) {
            Course course1 = new Course();
            course1.setCode("CSC101");
            course1.setName("Introduction to Programming");
            course1.setDescription("Learn the basics of programming");
            course1.setCredits(3);
            courseRepository.save(course1);

            Course course2 = new Course();
            course2.setCode("SEN201");
            course2.setName("Discrete Structures");
            course2.setDescription("Discrete structures");
            course2.setCredits(4);
            courseRepository.save(course2);

            log.info("Created sample courses");
        }

        log.info("Sample data loaded successfully!");

    }
}