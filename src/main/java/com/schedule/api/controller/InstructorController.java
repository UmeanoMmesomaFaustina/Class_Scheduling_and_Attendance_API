package com.schedule.api.controller;

import com.schedule.api.entity.Instructor;
import com.schedule.api.exception.ConflictException;
import com.schedule.api.exception.NotFoundException;
import com.schedule.api.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorRepository instructorRepository;

    @GetMapping
    public ResponseEntity<List<Instructor>> getAll() {
        return ResponseEntity.ok(instructorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instructor> getById(@PathVariable Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instructor not found"));
        return ResponseEntity.ok(instructor);
    }

    @PostMapping
    public ResponseEntity<Instructor> create(@RequestBody Instructor instructor) {
        if (instructor.getEmail() != null && instructorRepository.existsByEmail(instructor.getEmail())) {
            throw new ConflictException("Instructor with email already exists");
        }
        return new ResponseEntity<>(instructorRepository.save(instructor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Instructor> update(@PathVariable Long id, @RequestBody Instructor updated) {
        Instructor existing = instructorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instructor not found"));
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setDepartment(updated.getDepartment());
        return ResponseEntity.ok(instructorRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!instructorRepository.existsById(id)) {
            throw new NotFoundException("Instructor not found");
        }
        instructorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}