package com.luispdev.kotlin_service_template

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
@SpringBootTest
abstract class AbstractIntegrationTest {

    companion object {
        @Container
        var container: PostgreSQLContainer = PostgreSQLContainer("postgres:16-alpine")
            .withExposedPorts(5432)

        @DynamicPropertySource
        fun containersProperties(registry: DynamicPropertyRegistry) {
            container.start()
            registry.add("spring.datasource.url") { container.jdbcUrl }
            registry.add("spring.datasource.username") { container.username }
            registry.add("spring.datasource.password") { container.password }
        }
    }
}