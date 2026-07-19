package com.openclassrooms.starterjwt.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "yoga.messages")
public class YogaMessagesProperties {

    private Errors errors = new Errors();
    private Success success = new Success();

    @Data
    public static class Errors {
        private String badRequest;
        private String notFound;
        private String forbidden;
        private String unauthorized;
        private String unexpectedError;
        private String notAuthorized;
        private String invalidCredentials;
        private String internalServerError;
        private String invalidParameter;
        private String invalidRequest;
        private String unexpected;
        private String sessionNotFound;
        private String userNotFound;
        private String teacherNotFound;
        private String alreadyParticipating;
        private String notParticipating;
        private String userDeletionForbidden;
        private String emailAlreadyTaken;
    }

    @Data
    public static class Success {
        private String userRegistered;
    }
}
