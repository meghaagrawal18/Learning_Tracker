package com.example.learning_tracker_api.controller;

import com.example.learning_tracker_api.dto.CohortSummaryResponse;
import com.example.learning_tracker_api.service.SummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cohort-summary")
public class CohortSummaryController {

    private final SummaryService summaryService;

    public CohortSummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    public CohortSummaryResponse summary() {
        return summaryService.getSummary();
    }
}