package com.example.sensor_spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseConnectivityTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnection() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Database connected: " + conn.getMetaData().getURL());
            assertThat(conn.isValid(2)).isTrue(); // 2 秒超时
        }
    }
}
