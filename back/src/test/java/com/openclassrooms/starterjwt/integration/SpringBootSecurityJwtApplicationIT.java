package com.openclassrooms.starterjwt.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.openclassrooms.starterjwt.SpringBootSecurityJwtApplication;

@SpringBootTest(properties = {
        "oc.app.jwtSecret=B05rhIFhM+X6AeloFsPDBWdau6FuUwXK7sk08rjUN1lKEUdpXWHkOBHzXO8xYHyc98L2z78uu6h+W3+urd2Nlw==",
        "spring.docker.compose.enabled=false"
})
class SpringBootSecurityJwtApplicationIT extends IntegrationTestSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Vérifie que le contexte de l'application se charge correctement
        assertThat(applicationContext.getBean(SpringBootSecurityJwtApplication.class)).isNotNull();
    }
}
