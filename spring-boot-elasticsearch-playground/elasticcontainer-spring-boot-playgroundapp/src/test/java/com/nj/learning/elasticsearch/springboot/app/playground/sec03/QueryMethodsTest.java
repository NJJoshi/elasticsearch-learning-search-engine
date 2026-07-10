package com.nj.learning.elasticsearch.springboot.app.playground.sec03;

import com.nj.learning.elasticsearch.springboot.app.playground.AbstractTest;
import com.nj.learning.elasticsearch.springboot.app.playground.sec03.entity.Product;
import com.nj.learning.elasticsearch.springboot.app.playground.sec03.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;

import java.util.List;

public class QueryMethodsTest extends AbstractTest {
    private static final Logger log =  LoggerFactory.getLogger(QueryMethodsTest.class);

    @Autowired
    private ProductRepository productRepository;

    @BeforeAll
    public void dataSetup() {
        var products = this.readResource("sec03/products.json", new TypeReference<List<Product>>() {
        });
        this.productRepository.saveAll(products);
        Assertions.assertEquals(20, this.productRepository.count());
    }

    @Test
    public void findByCategory(){
        var searchHits = this.productRepository.findByCategory("Furniture");
        searchHits.forEach(this.print());
        Assertions.assertEquals(4, searchHits.getTotalHits());
    }

    @Test
    public void findByCategories(){
        var searchHits = this.productRepository.findByCategoryIn(List.of("Furniture", "Beauty"));
        searchHits.forEach(this.print());
        Assertions.assertEquals(8, searchHits.getTotalHits());
    }
}
