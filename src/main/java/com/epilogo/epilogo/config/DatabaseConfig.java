package com.epilogo.epilogo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${DB_HOST:localhost}")
    private String dbHost;

    @Value("${DB_PORT:5432}")
    private String dbPort;

    @Value("${DB_NAME:epilogo}")
    private String dbName;

    @Value("${DB_USERNAME:postgres}")
    private String dbUsername;

    @Value("${DB_PASSWORD:postgres}")
    private String dbPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
        System.out.println("Connecting to database URL: " + jdbcUrl);

        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);

        return dataSource;
    }
}