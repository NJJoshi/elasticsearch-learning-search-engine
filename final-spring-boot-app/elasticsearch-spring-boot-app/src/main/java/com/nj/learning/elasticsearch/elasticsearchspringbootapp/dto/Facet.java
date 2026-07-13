package com.nj.learning.elasticsearch.elasticsearchspringbootapp.dto;

import java.util.List;

public record Facet(String name,
                    List<FaceItem> items) {
}
