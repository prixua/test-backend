package com.example.testbackend.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@Hidden // Ocultar do Swagger pois são endpoints internos do Kubernetes
public class HealthController {

    private final ApplicationEventPublisher eventPublisher;

    public HealthController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/health/live")
    public ResponseEntity<Map<String, String>> liveness() {
        log.debug("Verificação de liveness executada");

        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("check", "liveness");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        log.debug("Verificação de readiness executada");

        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("check", "readiness");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Verificação geral de health executada");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "test-backend");
        response.put("version", "1.0.0");

        Map<String, String> checks = new HashMap<>();
        checks.put("liveness", "UP");
        checks.put("readiness", "UP");
        response.put("checks", checks);

        return ResponseEntity.ok(response);
    }
}
