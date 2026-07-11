package com.nj.learning.elasticsearch.springboot.app.playground.sec04.repository;

import com.nj.learning.elasticsearch.springboot.app.playground.sec04.entity.Article;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends ElasticsearchRepository<Article, String> {
    @Query("""
            {
              "multi_match" : {
                "query":"#{#searchTerm}",
                "fields":["title^3", "body"],
                "operator":"and",
                "fuzziness": 1,
                "type": "best_fields",
                "tie_breaker": 0.7
              }
            }
            """)
    @Highlight(fields = {
            @HighlightField(
                    name = "body",
                    parameters = @HighlightParameters(
                            preTags = "<b>",
                            postTags = "</b>"
                    )
            )
    })
    SearchHits<Article> search(String searchTerm);
}
