package com.openclassrooms.starterjwt.unit;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import com.openclassrooms.starterjwt.SpringBootSecurityJwtApplication;

@DisplayName("Given that the application starts")
class SpringBootSecurityJwtApplicationTest {

    @Test
    @DisplayName("When main is called, then Spring Boot should run the application")
    void shouldRunApplication() {
        String[] args = {};

        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            SpringBootSecurityJwtApplication.main(args);

            mockedSpringApplication.verify(
                    () -> SpringApplication.run(SpringBootSecurityJwtApplication.class, args));
        }
    }
}
