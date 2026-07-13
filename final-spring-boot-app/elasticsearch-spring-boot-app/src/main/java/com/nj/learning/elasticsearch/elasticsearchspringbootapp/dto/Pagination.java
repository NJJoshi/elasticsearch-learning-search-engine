package com.nj.learning.elasticsearch.elasticsearchspringbootapp.dto;

public record Pagination(int page,
                         int size,
                         long totalElements,
                         int totalPages) {
}
