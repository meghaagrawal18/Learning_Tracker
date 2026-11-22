package com.example.learning_tracker_api.service;
import com.example.learning_tracker_api.exception.ForbiddenException;

import com.example.learning_tracker_api.dto.ActivityResponse;
import com.example.learning_tracker_api.dto.AssignmentRequest;
import com.example.learning_tracker_api.dto.AssignmentResponse;
import com.example.learning_tracker_api.entity.Activity;
import com.example.learning_tracker_api.entity.Assignment;
import com.example.learning_tracker_api.entity.AssignmentStatus;
import com.example.learning_tracker_api.entity.Learner;
import com.example.learning_tracker_api.exception.ConflictException;
import com.example.learning_tracker_api.exception.ResourceNotFoundException;
import com.example.learning_tracker_api.repository.ActivityRepository;
import com.example.learning_tracker_api.repository.AssignmentRepository;
import com.example.learning_tracker_api.repository.LearnerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssignmentService {

    private final LearnerRepository learnerRepository;
    private final ActivityRepository activityRepository;
    private final AssignmentRepository assignmentRepository;

    public AssignmentService(LearnerRepository learnerRepository,
                             ActivityRepository activityRepository,
                             AssignmentRepository assignmentRepository) {
        this.learnerRepository = learnerRepository;
        this.activityRepository = activityRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<AssignmentResponse> findByLearner(Long learnerId) {
        if (!learnerRepository.existsById(learnerId)) {
            throw new ResourceNotFoundException("Learner not found.");
        }
        return assignmentRepository.findByLearnerId(learnerId).stream().map(this::toResponse).toList();
    }

    public AssignmentResponse create(AssignmentRequest request) {
        Learner learner = learnerRepository.findById(request.learnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found."));
        Activity activity = activityRepository.findById(request.activityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found."));

        if (assignmentRepository.existsByLearnerIdAndActivityId(learner.getId(), activity.getId())) {
            throw new ConflictException("Assignment already exists.");
        }

        Assignment assignment = new Assignment();
        assignment.setLearner(learner);
        assignment.setActivity(activity);
        assignment.setStatus(AssignmentStatus.NOT_STARTED);

        try {
            return toResponse(assignmentRepository.save(assignment));
        } catch (DataIntegrityViolationException e) {
            // Two requests at the same moment: the database constraint is the safety net
            throw new ConflictException("Assignment already exists.");
        }
    }

    public AssignmentResponse updateStatus(Long assignmentId, AssignmentStatus status) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found."));
        assignment.setStatus(status);
        return toResponse(assignmentRepository.save(assignment));
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        Activity activity = assignment.getActivity();
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getLearner().getId(),
                new ActivityResponse(activity.getId(), activity.getTitle(),
                        activity.getTopic(), activity.getPlannedHours()),
                assignment.getStatus());
    }
    public List<AssignmentResponse> findForAccount(Long accountId) {
        Learner learner = learnerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found."));
        return findByLearner(learner.getId());
    }

    public AssignmentResponse updateOwnStatus(Long assignmentId, AssignmentStatus status, Long accountId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found."));
        Learner learner = learnerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ForbiddenException("Access denied."));

        if (!assignment.getLearner().getId().equals(learner.getId())) {
            throw new ForbiddenException("Access denied.");
        }
        assignment.setStatus(status);
        return toResponse(assignmentRepository.save(assignment));
    }
}