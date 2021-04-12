package org.kafka.demo.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.core.instrument.binder.db.PostgreSQLDatabaseMetrics;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource({ "classpath:application.properties" })
public class AppConfiguration {

    @Bean
    @Autowired
    public PostgreSQLDatabaseMetrics dbmetric(DataSource dataSource) {
        return new PostgreSQLDatabaseMetrics(dataSource,"postgres");
    }
}
