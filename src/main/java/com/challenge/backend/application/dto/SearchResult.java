package com.challenge.backend.application.dto;

import com.challenge.backend.domain.model.Solicitation;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResult {
    private List<Solicitation> items;
    private int page;
    private int size;
    private long total;
}