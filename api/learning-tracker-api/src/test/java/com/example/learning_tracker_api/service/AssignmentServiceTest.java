package com.example.learning_tracker_api.service;

import com.example.learning_tracker_api.dto.AssignmentRequest;
import com.example.learning_tracker_api.dto.AssignmentResponse;
import com.example.learning_tracker_api.dto.CohortSummaryResponse;
import com.example.learning_tracker_api.entity.AssignmentStatus;
import com.example.learning_tracker_api.exception.ConflictException;
import com.example.learning_tracker_api.exception.ResourceNotFoundException;
import com.example.learning_tracker_api.repository.ActivityRepository;
import com.example.learning_tracker_api.repository.LearnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:assignmenttest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AssignmentServiceTest {

    @Autowired AssignmentService assignmentService;
    @Autowired SummaryService summaryService;
    @Autowired LearnerRepository learnerRepository;
    @Autowired ActivityRepository activityRepository;

    private Long learnerId() { return learnerRepository.findAll().get(0).getId(); }
    private Long activityId() { return activityRepository.findAll().get(0).getId(); }

    @Test
    void assigningCreatesNotStartedAssignment() {
        AssignmentResponse created = assignmentService.create(new AssignmentRequest(learnerId(), activityId()));
        assertEquals(AssignmentStatus.NOT_STARTED, created.status());
    }

    @Test
    void duplicateAssignmentIsRejected() {
        assignmentService.create(new AssignmentRequest(learnerId(), activityId()));
        assertThrows(ConflictException.class,
                () -> assignmentService.create(new AssignmentRequest(learnerId(), activityId())));
    }

    @Test
    void unknownLearnerIsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.create(new AssignmentRequest(999999L, activityId())));
    }

    @Test
    void unknownActivityIsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.create(new AssignmentRequest(learnerId(), 999999L)));
    }

    @Test
    void statusCanBeUpdated() {
        AssignmentResponse created = assignmentService.create(new AssignmentRequest(learnerId(), activityId()));
        AssignmentResponse updated = assignmentService.updateStatus(created.id(), AssignmentStatus.IN_PROGRESS);
        assertEquals(AssignmentStatus.IN_PROGRESS, updated.status());
    }

    @Test
    void updatingMissingAssignmentIsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.updateStatus(999999L, AssignmentStatus.COMPLETED));
    }

    @Test
    void summaryCountsCompletedAssignments() {
        AssignmentResponse created = assignmentService.create(new AssignmentRequest(learnerId(), activityId()));
        assignmentService.updateStatus(created.id(), AssignmentStatus.COMPLETED);
        CohortSummaryResponse summary = summaryService.getSummary();
        assertEquals(1, summary.assignments());
        assertEquals(1, summary.completed());
    }
}