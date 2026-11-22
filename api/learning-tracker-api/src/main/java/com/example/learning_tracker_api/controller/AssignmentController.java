package com.example.learning_tracker_api.controller;

import com.example.learning_tracker_api.dto.AssignmentRequest;
import com.example.learning_tracker_api.dto.AssignmentResponse;
import com.example.learning_tracker_api.dto.StatusRequest;
import com.example.learning_tracker_api.security.AccountPrincipal;
import com.example.learning_tracker_api.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(@Valid @RequestBody AssignmentRequest request) {
        return assignmentService.create(request);
    }

    @PatchMapping("/{id}/status")
    public AssignmentResponse updateStatus(@PathVariable Long id,
                                           @Valid @RequestBody StatusRequest request,
                                           @AuthenticationPrincipal AccountPrincipal principal) {
        return assignmentService.updateOwnStatus(id, request.status(), principal.accountId());
    }
}