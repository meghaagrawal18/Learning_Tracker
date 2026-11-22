package com.example.learning_tracker_api.service;

import com.example.learning_tracker_api.dto.CohortSummaryResponse;
import com.example.learning_tracker_api.entity.AssignmentStatus;
import com.example.learning_tracker_api.repository.AssignmentRepository;
import com.example.learning_tracker_api.repository.LearnerRepository;
import org.springframework.stereotype.Service;

@Service
public class SummaryService {

    private final LearnerRepository learnerRepository;
    private final AssignmentRepository assignmentRepository;

    public SummaryService(LearnerRepository learnerRepository, AssignmentRepository assignmentRepository) {
        this.learnerRepository = learnerRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public CohortSummaryResponse getSummary() {
        return new CohortSummaryResponse(
                learnerRepository.count(),
                assignmentRepository.count(),
                assignmentRepository.countByStatus(AssignmentStatus.COMPLETED));
    }
}