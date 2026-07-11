package com.nj.learning.elasticsearch.springboot.app.playground.sec04;

import com.nj.learning.elasticsearch.springboot.app.playground.AbstractTest;
import com.nj.learning.elasticsearch.springboot.app.playground.sec04.entity.Article;
import com.nj.learning.elasticsearch.springboot.app.playground.sec04.repository.ArticleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;

import java.util.List;

public class QueryAnnotationTest extends AbstractTest {
    @Autowired
    private ArticleRepository articleRepository;

    @BeforeAll
    public void dataSetup() {
        var articles = this.readResource("sec04/articles.json", new TypeReference<List<Article>>() {
        });
        this.articleRepository.saveAll(articles);
        Assertions.assertEquals(11, this.articleRepository.count());
    }

    @Test
    public void searchArticles() {
        var searchHits = this.articleRepository.search("spring season");
        searchHits.forEach(this.print());
        Assertions.assertEquals(4, searchHits.getTotalHits());
    }
}
