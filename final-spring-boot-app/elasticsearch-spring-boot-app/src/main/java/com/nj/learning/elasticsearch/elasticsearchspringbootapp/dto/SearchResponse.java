package com.nj.learning.elasticsearch.elasticsearchspringbootapp.dto;

import java.util.List;

public record SearchResponse(List<Business> results,
                             List<Facet> facets,
                             Pagination pagination,
                             long timeTaken) {
}
