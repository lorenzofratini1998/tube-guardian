package io.tubeguardian.orchestrator.configuration;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "io.tubeguardian")
@EntityScan(basePackages = "io.tubeguardian")
@ComponentScan(basePackages = "io.tubeguardian")
public class VideoOrchestratorConfiguration {}
