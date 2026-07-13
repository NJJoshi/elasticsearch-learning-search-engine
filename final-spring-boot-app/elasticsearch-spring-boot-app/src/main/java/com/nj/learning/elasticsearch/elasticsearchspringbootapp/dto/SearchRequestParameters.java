package com.nj.learning.elasticsearch.elasticsearchspringbootapp.dto;

import com.nj.learning.elasticsearch.elasticsearchspringbootapp.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.Objects;

public record SearchRequestParameters(String query,
                                      String distance,
                                      Double latitude,
                                      Double longitude,
                                      Double rating,
                                      String state,
                                      String offerings,
                                      Integer page,
                                      Integer size) {
    public SearchRequestParameters {
        if(!StringUtils.hasText(query)) {
            throw new BadRequestException("Query parameter is required");
        }
        page = Objects.requireNonNullElse(page, 0);
        size = Objects.requireNonNullElse(size, 10);
    }
}
