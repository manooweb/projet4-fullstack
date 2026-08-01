package com.openclassrooms.starterjwt.integration.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
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
        return createTestSession(name, date, description, teacher, List.of());
    }

    public Session createTestSession(
            String name,
            Date date,
            String description,
            Teacher teacher,
            List<User> users) {
        Session session = new Session()
                .setName(name)
                .setDate(date)
                .setDescription(description)
                .setTeacher(teacher)
                .setUsers(new ArrayList<>(users));

        return sessionRepository.save(session);
    }
}
