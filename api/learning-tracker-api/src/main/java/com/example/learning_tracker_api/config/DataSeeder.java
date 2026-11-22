package com.example.learning_tracker_api.config;

import com.example.learning_tracker_api.entity.*;
import com.example.learning_tracker_api.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AccountRepository accountRepository;
    private final LearnerRepository learnerRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.coordinator.email:}")
    private String coordinatorEmail;

    @Value("${app.coordinator.password:}")
    private String coordinatorPassword;

    public DataSeeder(AccountRepository accountRepository, LearnerRepository learnerRepository,
                      ActivityRepository activityRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.learnerRepository = learnerRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedCoordinator();
        seedActivities();
        seedLearners();
    }

    private void seedCoordinator() {
        if (accountRepository.existsByRole(Role.COORDINATOR)) {
            return;
        }
        if (coordinatorEmail.isBlank() || coordinatorPassword.isBlank()) {
            log.warn("No coordinator exists. Set COORDINATOR_EMAIL and COORDINATOR_PASSWORD and restart.");
            return;
        }
        Account account = new Account();
        account.setEmail(coordinatorEmail);
        account.setPasswordHash(passwordEncoder.encode(coordinatorPassword));
        account.setRole(Role.COORDINATOR);
        account.setMustChangePassword(false);
        accountRepository.save(account);
        log.info("Initial coordinator account created.");
    }

    private void seedActivities() {
        if (activityRepository.count() > 0) {
            return;
        }
        saveActivity("React Basics", "Frontend", 6);
        saveActivity("REST APIs", "Backend", 8);
        saveActivity("SQL Foundations", "Data", 4);
        saveActivity("Spring Boot Basics", "Backend", 10);
    }

    private void saveActivity(String title, String topic, double hours) {
        Activity activity = new Activity();
        activity.setTitle(title);
        activity.setTopic(topic);
        activity.setPlannedHours(hours);
        activityRepository.save(activity);
    }

    private void seedLearners() {
        if (learnerRepository.count() > 0) {
            return;
        }
        saveLearner("Jordan Lee", "jordan.lee@example.com");
        saveLearner("Priya Nair", "priya.nair@example.com");
        saveLearner("Sam Carter", "sam.carter@example.com");
    }

    private void saveLearner(String name, String email) {
        // Seeded candidates get an unusable random password and an already-expired
        // temporary password. The coordinator issues real credentials via reset (Day 3).
        Account account = new Account();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        account.setRole(Role.CANDIDATE);
        account.setMustChangePassword(true);
        account.setTempPasswordExpiresAt(Instant.now());
        accountRepository.save(account);

        Learner learner = new Learner();
        learner.setName(name);
        learner.setCohort("Frontend cohort");
        learner.setAccount(account);
        learnerRepository.save(learner);
    }
}