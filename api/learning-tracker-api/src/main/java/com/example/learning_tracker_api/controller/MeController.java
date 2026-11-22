package com.example.learning_tracker_api.controller;

import com.example.learning_tracker_api.dto.AssignmentResponse;
import com.example.learning_tracker_api.security.AccountPrincipal;
import com.example.learning_tracker_api.service.AssignmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AssignmentService assignmentService;

    public MeController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/assignments")
    public List<AssignmentResponse> myAssignments(@AuthenticationPrincipal AccountPrincipal principal) {
        return assignmentService.findForAccount(principal.accountId());
    }
}