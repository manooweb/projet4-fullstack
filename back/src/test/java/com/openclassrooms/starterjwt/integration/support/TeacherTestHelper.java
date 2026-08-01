package com.openclassrooms.starterjwt.integration.support;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;

public class TeacherTestHelper {

    private final TeacherRepository teacherRepository;

    public TeacherTestHelper(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public Teacher createTestTeacher(String firstName, String lastName) {
        Teacher teacher = new Teacher()
                .setFirstName(firstName)
                .setLastName(lastName);

        return teacherRepository.save(teacher);
    }
}
