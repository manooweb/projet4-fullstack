package com.openclassrooms.starterjwt.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest(properties = {
        "oc.app.jwtSecret=B05rhIFhM+X6AeloFsPDBWdau6FuUwXK7sk08rjUN1lKEUdpXWHkOBHzXO8xYHyc98L2z78uu6h+W3+urd2Nlw==",
        "spring.docker.compose.enabled=false"
})
public abstract class IntegrationTestSupport {

    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:9.7")
            .withDatabaseName("yoga_test")
            .withUsername("yoga_test")
            .withPassword("yoga_test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        MYSQL.start();
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}
