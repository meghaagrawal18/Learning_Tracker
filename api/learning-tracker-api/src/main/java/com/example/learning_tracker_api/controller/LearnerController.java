package com.example.learning_tracker_api.controller;

import com.example.learning_tracker_api.dto.AssignmentResponse;
import com.example.learning_tracker_api.dto.CandidateCreatedResponse;
import com.example.learning_tracker_api.dto.CredentialsResponse;
import com.example.learning_tracker_api.dto.LearnerRequest;
import com.example.learning_tracker_api.dto.LearnerResponse;
import com.example.learning_tracker_api.service.AssignmentService;
import com.example.learning_tracker_api.service.LearnerService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learners")
public class LearnerController {

    private final LearnerService learnerService;
    private final AssignmentService assignmentService;

    public LearnerController(LearnerService learnerService, AssignmentService assignmentService) {
        this.learnerService = learnerService;
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<LearnerResponse> list() {
        return learnerService.findAll();
    }

    @PostMapping
    public ResponseEntity<CandidateCreatedResponse> create(@Valid @RequestBody LearnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(learnerService.create(request));
    }

    @GetMapping("/{id}/assignments")
    public List<AssignmentResponse> assignments(@PathVariable Long id) {
        return assignmentService.findByLearner(id);
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<CredentialsResponse> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(learnerService.resetPassword(id));
    }
}