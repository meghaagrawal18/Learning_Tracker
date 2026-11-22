package com.example.learning_tracker_api.service;

import com.example.learning_tracker_api.dto.ActivityRequest;
import com.example.learning_tracker_api.dto.ActivityResponse;
import com.example.learning_tracker_api.entity.Activity;
import com.example.learning_tracker_api.exception.ResourceNotFoundException;
import com.example.learning_tracker_api.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<ActivityResponse> findAll() {
        return activityRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ActivityResponse create(ActivityRequest request) {
        Activity activity = new Activity();
        apply(activity, request);
        return toResponse(activityRepository.save(activity));
    }

    public ActivityResponse update(Long id, ActivityRequest request) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found."));
        apply(activity, request);
        return toResponse(activityRepository.save(activity));
    }

    private void apply(Activity activity, ActivityRequest request) {
        activity.setTitle(request.title().trim());
        activity.setTopic(request.topic().trim());
        activity.setPlannedHours(request.plannedHours());
    }

    private ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getTitle(),
                activity.getTopic(), activity.getPlannedHours());
    }
}