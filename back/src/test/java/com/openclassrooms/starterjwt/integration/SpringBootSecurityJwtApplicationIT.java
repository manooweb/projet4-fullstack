package com.openclassrooms.starterjwt.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.openclassrooms.starterjwt.SpringBootSecurityJwtApplication;

class SpringBootSecurityJwtApplicationIT extends IntegrationTestSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Vérifie que le contexte de l'application se charge correctement
        assertThat(applicationContext.getBean(SpringBootSecurityJwtApplication.class)).isNotNull();
    }
}
