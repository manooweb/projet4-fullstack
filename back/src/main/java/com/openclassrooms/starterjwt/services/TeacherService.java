package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.configuration.YogaProperties;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final YogaProperties yogaProperties;

    public TeacherService(TeacherRepository teacherRepository, YogaProperties yogaProperties) {
        this.teacherRepository = teacherRepository;
        this.yogaProperties = yogaProperties;
    }

    public List<Teacher> findAll() {
        return this.teacherRepository.findAll();
    }

    public Teacher findById(Long id) {
        return this.teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        yogaProperties.getMessages().getErrors().getTeacherNotFound().formatted(id)));
    }
}
