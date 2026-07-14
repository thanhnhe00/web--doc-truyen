package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.ReportRequest;
import com.storyreading.storyreadingbackend.entity.Report;
import com.storyreading.storyreadingbackend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Report> create(@Valid @RequestBody ReportRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(request, authentication));
    }
}