package com.example.learning_tracker_api.service;

import com.example.learning_tracker_api.dto.AssignmentRequest;
import com.example.learning_tracker_api.dto.AssignmentResponse;
import com.example.learning_tracker_api.entity.AssignmentStatus;
import com.example.learning_tracker_api.entity.Learner;
import com.example.learning_tracker_api.exception.ForbiddenException;
import com.example.learning_tracker_api.exception.ResourceNotFoundException;
import com.example.learning_tracker_api.repository.ActivityRepository;
import com.example.learning_tracker_api.repository.LearnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:ownershiptest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AssignmentOwnershipTest {

    @Autowired AssignmentService assignmentService;
    @Autowired LearnerRepository learnerRepository;
    @Autowired ActivityRepository activityRepository;

    @Test
    void ownerCanUpdateOwnAssignment() {
        List<Learner> learners = learnerRepository.findAll();
        AssignmentResponse created = assignmentService.create(
                new AssignmentRequest(learners.get(0).getId(), activityRepository.findAll().get(0).getId()));

        AssignmentResponse updated = assignmentService.updateOwnStatus(
                created.id(), AssignmentStatus.COMPLETED, learners.get(0).getAccount().getId());
        assertEquals(AssignmentStatus.COMPLETED, updated.status());
    }

    @Test
    void anotherCandidateCannotUpdateIt() {
        List<Learner> learners = learnerRepository.findAll();
        AssignmentResponse created = assignmentService.create(
                new AssignmentRequest(learners.get(0).getId(), activityRepository.findAll().get(0).getId()));

        assertThrows(ForbiddenException.class, () -> assignmentService.updateOwnStatus(
                created.id(), AssignmentStatus.COMPLETED, learners.get(1).getAccount().getId()));
    }

    @Test
    void missingAssignmentIsNotFound() {
        Learner learner = learnerRepository.findAll().get(0);
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.updateOwnStatus(
                999999L, AssignmentStatus.COMPLETED, learner.getAccount().getId()));
    }

    @Test
    void myAssignmentsOnlyShowsMyOwn() {
        List<Learner> learners = learnerRepository.findAll();
        assignmentService.create(
                new AssignmentRequest(learners.get(0).getId(), activityRepository.findAll().get(0).getId()));

        assertEquals(1, assignmentService.findForAccount(learners.get(0).getAccount().getId()).size());
        assertEquals(0, assignmentService.findForAccount(learners.get(1).getAccount().getId()).size());
    }
}