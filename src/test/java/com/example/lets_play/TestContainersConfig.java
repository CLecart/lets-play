package com.example.lets_play;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

public class TestContainersConfig {

    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @BeforeAll
    public static void startContainer() {
        mongo.start();
    }

    @AfterAll
    public static void stopContainer() {
        mongo.stop();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongo.getReplicaSetUrl());
    }
}
