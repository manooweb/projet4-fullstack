package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.configuration.YogaProperties;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    private final UserRepository userRepository;
    private final YogaProperties yogaProperties;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository,
            YogaProperties yogaProperties) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.yogaProperties = yogaProperties;
    }

    public Session create(Session session) {
        return this.sessionRepository.save(session);
    }

    public void delete(Long id) {
        this.sessionRepository.deleteById(id);
    }

    public List<Session> findAll() {
        return this.sessionRepository.findAll();
    }

    public Session getById(Long id) {
        return this.sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(sessionNotFoundMessage(id)));
    }

    public Session update(Long id, Session session) {
        sessionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                    sessionNotFoundMessage(id)
            ));

        session.setId(id);
        return this.sessionRepository.save(session);
    }

    public void participate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(sessionNotFoundMessage(id)));
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        yogaProperties.getMessages().getErrors().getUserNotFound().formatted(userId)));

        boolean alreadyParticipate = session.getUsers().stream().anyMatch(o -> o.getId().equals(userId));
        if (alreadyParticipate) {
            throw new BadRequestException(
                    yogaProperties.getMessages().getErrors().getAlreadyParticipating().formatted(userId, id));
        }

        session.getUsers().add(user);

        this.sessionRepository.save(session);
    }

    public void noLongerParticipate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(sessionNotFoundMessage(id)));

        boolean alreadyParticipate = session.getUsers().stream().anyMatch(o -> o.getId().equals(userId));
        if (!alreadyParticipate) {
            throw new BadRequestException(
                    yogaProperties.getMessages().getErrors().getNotParticipating().formatted(userId, id));
        }

        session.setUsers(
                session.getUsers().stream().filter(user -> !user.getId().equals(userId)).collect(Collectors.toList()));

        this.sessionRepository.save(session);
    }

    private String sessionNotFoundMessage(Long id) {
        return yogaProperties.getMessages().getErrors().getSessionNotFound().formatted(id);
    }
}
