package com.openclassrooms.starterjwt.integration.support;

import java.util.ArrayList;
import java.util.Date;

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.SessionRepository;

public class SessionTestHelper {

    private final SessionRepository sessionRepository;

    public SessionTestHelper(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createTestSession(
            String name,
            Date date,
            String description,
            Teacher teacher) {
        Session session = new Session()
                .setName(name)
                .setDate(date)
                .setDescription(description)
                .setTeacher(teacher)
                .setUsers(new ArrayList<>());

        return sessionRepository.save(session);
    }
}
