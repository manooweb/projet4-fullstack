package com.openclassrooms.starterjwt.unit.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.ConflictException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.SessionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Given that a session service")
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        YogaMessagesProperties messages = new YogaMessagesProperties();
        messages.getErrors().setSessionNotFound("Session with id %d was not found.");
        messages.getErrors().setUserNotFound("User with id %d was not found.");
        messages.getErrors().setTeacherAlreadyAssigned(
                "Teacher with id %d is already assigned to the session with id %d.");
        messages.getErrors().setAlreadyParticipating(
                "User with id %d already participate to the session with id %d.");
        messages.getErrors().setNotParticipating(
                "User with id %d is not participating in the session with id %d.");

        sessionService = new SessionService(
                sessionRepository,
                userRepository,
                new YogaProperties(messages));
    }

    @Nested
    @DisplayName("Given a session to create")
    class CreateTests {

        @Test
        @DisplayName("When the session is created, then the saved session should be returned")
        void shouldCreateSession() {
            Teacher teacher = new Teacher().setId(1L);
            Session session = session(1L).setTeacher(teacher);
            when(sessionRepository.findByTeacherId(teacher.getId())).thenReturn(Optional.empty());
            when(sessionRepository.save(session)).thenReturn(session);

            Session createdSession = sessionService.create(session);

            assertSame(session, createdSession);
            verify(sessionRepository).save(session);
        }

        @Test
        @DisplayName("When the teacher is already assigned to another session, then a conflict exception should be thrown")
        void shouldThrowConflictExceptionWhenTeacherIsAlreadyAssigned() {
            Teacher teacher = new Teacher().setId(1L);
            Session existingSession = session(2L).setTeacher(teacher);
            Session sessionToCreate = session(null).setTeacher(teacher);
            when(sessionRepository.findByTeacherId(teacher.getId())).thenReturn(Optional.of(existingSession));

            ConflictException exception = assertThrows(
                    ConflictException.class,
                    () -> sessionService.create(sessionToCreate));

            assertEquals(
                    "Teacher with id 1 is already assigned to the session with id 2.",
                    exception.getMessage());
            verify(sessionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Given sessions to retrieve")
    class ReadTests {

        @Test
        @DisplayName("When all sessions are requested, then all sessions should be returned")
        void shouldFindAllSessions() {
            List<Session> sessions = List.of(session(1L), session(2L));
            when(sessionRepository.findAll()).thenReturn(sessions);

            List<Session> foundSessions = sessionService.findAll();

            assertEquals(sessions, foundSessions);
            verify(sessionRepository).findAll();
        }

        @Test
        @DisplayName("When an existing session is requested by ID, then the session should be returned")
        void shouldReturnSessionById() {
            Session session = session(1L);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

            Session foundSession = sessionService.getById(1L);

            assertSame(session, foundSession);
            verify(sessionRepository).findById(1L);
        }

        @Test
        @DisplayName("When an unknown session is requested by ID, then a not found exception should be thrown")
        void shouldThrowNotFoundExceptionForUnknownSession() {
            long unknownSessionId = 99L;
            when(sessionRepository.findById(unknownSessionId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> sessionService.getById(unknownSessionId));

            assertEquals("Session with id 99 was not found.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Given a session to update")
    class UpdateTests {

        @Test
        @DisplayName("When the teacher is already assigned to the session being updated, then the update should succeed")
        void shouldUpdateSessionWhenTeacherIsAlreadyAssignedToIt() {
            long sessionId = 1L;
            Teacher teacher = new Teacher().setId(1L);
            Session existingSession = session(sessionId).setTeacher(teacher);
            Session updatedSession = session(null).setTeacher(teacher).setName("Updated yoga");
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
            when(sessionRepository.findByTeacherId(teacher.getId())).thenReturn(Optional.of(existingSession));
            when(sessionRepository.save(updatedSession)).thenReturn(updatedSession);

            Session result = sessionService.update(sessionId, updatedSession);

            assertSame(updatedSession, result);
            assertEquals(sessionId, updatedSession.getId());
            verify(sessionRepository).save(updatedSession);
        }

        @Test
        @DisplayName("When the teacher is assigned to another session, then a conflict exception should be thrown")
        void shouldThrowConflictExceptionWhenUpdatingWithTeacherAssignedToAnotherSession() {
            long sessionId = 1L;
            Teacher teacher = new Teacher().setId(1L);
            Session sessionToUpdate = session(sessionId).setTeacher(new Teacher().setId(2L));
            Session otherSession = session(2L).setTeacher(teacher);
            Session updatedSession = session(null).setTeacher(teacher);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(sessionToUpdate));
            when(sessionRepository.findByTeacherId(teacher.getId())).thenReturn(Optional.of(otherSession));

            ConflictException exception = assertThrows(
                    ConflictException.class,
                    () -> sessionService.update(sessionId, updatedSession));

            assertEquals(
                    "Teacher with id 1 is already assigned to the session with id 2.",
                    exception.getMessage());
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("When the session does not exist, then it should not be saved")
        void shouldNotUpdateUnknownSession() {
            long unknownSessionId = 99L;
            Session updatedSession = session(null);
            when(sessionRepository.findById(unknownSessionId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> sessionService.update(unknownSessionId, updatedSession));

            assertEquals("Session with id 99 was not found.", exception.getMessage());
            verify(sessionRepository, never()).save(any(Session.class));
        }
    }

    @Nested
    @DisplayName("Given a session to delete")
    class DeleteTests {

        @Test
        @DisplayName("When the session exists, then the session should be deleted")
        void shouldDeleteExistingSession() {
            long sessionId = 1L;
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session(sessionId)));

            sessionService.delete(sessionId);

            verify(sessionRepository).deleteById(sessionId);
        }

        @Test
        @DisplayName("When the session does not exist, then it should not be deleted")
        void shouldNotDeleteUnknownSession() {
            long unknownSessionId = 99L;
            when(sessionRepository.findById(unknownSessionId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> sessionService.delete(unknownSessionId));

            assertEquals("Session with id 99 was not found.", exception.getMessage());
            verify(sessionRepository, never()).deleteById(unknownSessionId);
        }
    }

    @Nested
    @DisplayName("Given a user who wants to participate in a session")
    class ParticipateTests {

        @Test
        @DisplayName("When the session and user exist and the user is not participating, then participation should be saved")
        void shouldAddUserToSessionParticipants() {
            long sessionId = 1L;
            long userId = 2L;
            Session session = session(sessionId);
            User user = user(userId);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            sessionService.participate(sessionId, userId);

            assertTrue(session.getUsers().contains(user));
            verify(sessionRepository).save(session);
        }

        @Test
        @DisplayName("When the session does not exist, then participation should not query the user")
        void shouldNotParticipateInUnknownSession() {
            long unknownSessionId = 99L;
            when(sessionRepository.findById(unknownSessionId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> sessionService.participate(unknownSessionId, 2L));

            assertEquals("Session with id 99 was not found.", exception.getMessage());
            verifyNoInteractions(userRepository);
            verify(sessionRepository, never()).save(any(Session.class));
        }

        @Test
        @DisplayName("When the user does not exist, then participation should not be saved")
        void shouldNotParticipateWithUnknownUser() {
            long sessionId = 1L;
            long unknownUserId = 99L;
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session(sessionId)));
            when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> sessionService.participate(sessionId, unknownUserId));

            assertEquals("User with id 99 was not found.", exception.getMessage());
            verify(sessionRepository, never()).save(any(Session.class));
        }

        @Test
        @DisplayName("When the user already participates, then participation should not be saved twice")
        void shouldNotParticipateTwice() {
            long sessionId = 1L;
            long userId = 2L;
            User user = user(userId);
            Session session = session(sessionId, user);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> sessionService.participate(sessionId, userId));

            assertEquals(
                    "User with id 2 already participate to the session with id 1.",
                    exception.getMessage());
            assertEquals(1, session.getUsers().size());
            verify(sessionRepository, never()).save(any(Session.class));
        }
    }

    @Nested
    @DisplayName("Given a user who wants to leave a session")
    class NoLongerParticipateTests {

        @Test
        @DisplayName("When the user participates, then the user should be removed from the session")
        void shouldRemoveUserFromSessionParticipants() {
            long sessionId = 1L;
            User userToRemove = user(2L);
            User remainingUser = user(3L);
            Session session = session(sessionId, userToRemove, remainingUser);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

            sessionService.noLongerParticipate(sessionId, userToRemove.getId());

            assertEquals(1, session.getUsers().size());
            assertEquals(remainingUser.getId(), session.getUsers().getFirst().getId());
            verify(sessionRepository).save(session);
        }

        @Test
        @DisplayName("When the session does not exist, then withdrawal should not query the user")
        void shouldNotLeaveUnknownSession() {
            long unknownSessionId = 99L;
            when(sessionRepository.findById(unknownSessionId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> sessionService.noLongerParticipate(unknownSessionId, 2L));

            assertEquals("Session with id 99 was not found.", exception.getMessage());
            verifyNoInteractions(userRepository);
            verify(sessionRepository, never()).save(any(Session.class));
        }

        @Test
        @DisplayName("When the user does not participate, then withdrawal should not be saved")
        void shouldNotLeaveSessionWhenUserDoesNotParticipate() {
            long sessionId = 1L;
            long unknownUserId = 99L;
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session(sessionId)));

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> sessionService.noLongerParticipate(sessionId, unknownUserId));

            assertEquals(
                    "User with id 99 is not participating in the session with id 1.",
                    exception.getMessage());
            verifyNoInteractions(userRepository);
            verify(sessionRepository, never()).save(any(Session.class));
        }
    }

    private Session session(Long id, User... users) {
        return new Session()
                .setId(id)
                .setUsers(new ArrayList<>(List.of(users)));
    }

    private User user(Long id) {
        return new User().setId(id);
    }
}
