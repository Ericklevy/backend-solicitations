package com.challenge.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SearchCriteria {
    private String q;
    private List<String> status;
    private String serviceType;
    private String priority;
    private String state;
    private Instant dateFrom;
    private Instant dateTo;
    private int page;
    private int size;
    private String sortField;
    private String sortDirection;
}