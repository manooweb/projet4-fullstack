package com.openclassrooms.starterjwt.unit.teacher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.starterjwt.configuration.properties.YogaMessagesProperties;
import com.openclassrooms.starterjwt.configuration.properties.YogaProperties;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.services.TeacherService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Given that a teacher service")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;
    private YogaProperties yogaProperties;
    private TeacherService teacherService;

    @BeforeEach
    void setUp() {
        YogaMessagesProperties messages = new YogaMessagesProperties();
        messages.getErrors().setTeacherNotFound("Teacher with id %s was not found.");

        yogaProperties = new YogaProperties(messages);
        teacherService = new TeacherService(teacherRepository, yogaProperties);

    }

    @DisplayName("When calling findAll, then all teachers are returned")
    @Test
    void testFindAll() {
        Teacher teacher = new Teacher()
                .setId(1L)
                .setFirstName("John")
                .setLastName("Doe");
        List<Teacher> expectedTeachers = List.of(teacher);

        when(teacherRepository.findAll()).thenReturn(expectedTeachers);

        List<Teacher> actualTeachers = teacherService.findAll();

        assertThat(actualTeachers).isEqualTo(expectedTeachers);
        verify(teacherRepository, times(1)).findAll();
    }

    @Nested
    @DisplayName("Given that an existiing teacher")
    class ExistingTeacher {

        @DisplayName("When requested by id, then the teacher is returned")
        @Test
        void shouldReturnTeacherWhenFound() {
            Teacher teacher = new Teacher()
                    .setId(1L)
                    .setFirstName("John")
                    .setLastName("Doe");
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

            Teacher actualTeacher = teacherService.findById(1L);

            assertThat(actualTeacher).isEqualTo(teacher);
            verify(teacherRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Given that an unknown teacher")
    class UnknownTeacher {

        @DisplayName("When requested by id, then it throws a NotFoundException")
        @Test
        void shouldThrowNotFoundException() {
            Long unknownTeacherId = 99L;

            when(teacherRepository.findById(unknownTeacherId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> teacherService.findById(unknownTeacherId));

            assertThat(exception.getMessage()).isEqualTo("Teacher with id %d was not found.".formatted(unknownTeacherId));

            verify(teacherRepository, times(1)).findById(unknownTeacherId);
        }
    }
}
