package com.example.desafio_santander.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.redis.host=localhost",
    "spring.redis.port=6379",
    "app.load-sample-data=false"
})
class DesafioSantanderIntegrationTest {

    @Test
    void testApplicationContextLoads() {
        // Este teste verifica se o contexto da aplicação carrega corretamente
    }
}
