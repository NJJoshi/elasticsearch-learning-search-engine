package com.nj.learning.elasticsearch.elasticsearchspringbootapp.controller;

import com.nj.learning.elasticsearch.elasticsearchspringbootapp.dto.SearchRequestParameters;
import com.nj.learning.elasticsearch.elasticsearchspringbootapp.dto.SearchResponse;
import com.nj.learning.elasticsearch.elasticsearchspringbootapp.dto.SuggestionRequestParameters;
import com.nj.learning.elasticsearch.elasticsearchspringbootapp.service.SearchService;
import com.nj.learning.elasticsearch.elasticsearchspringbootapp.service.SuggestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BusinessSearchController {

    private final SearchService searchService;
    private final SuggestionService suggestionService;

    public BusinessSearchController(SearchService searchService, SuggestionService suggestionService) {
        this.searchService = searchService;
        this.suggestionService = suggestionService;
    }

    @GetMapping("/api/suggestions")
    public List<String> suggest(SuggestionRequestParameters parameters){
        return this.suggestionService.fetchSuggestions(parameters);
    }

    @GetMapping("/api/search")
    public SearchResponse search(SearchRequestParameters parameters){
        return this.searchService.search(parameters);
    }
}
