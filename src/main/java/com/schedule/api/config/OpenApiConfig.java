package com.schedule.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schedulingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Class Scheduling & Attendance API")
                        .description("API for managing class schedules, clash detection, and attendance tracking")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Scheduling Team")
                                .email("team@university.edu")));
    }
}