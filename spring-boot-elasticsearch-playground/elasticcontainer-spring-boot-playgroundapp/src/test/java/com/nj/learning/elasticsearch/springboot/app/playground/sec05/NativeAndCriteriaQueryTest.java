package com.nj.learning.elasticsearch.springboot.app.playground.sec05;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.nj.learning.elasticsearch.springboot.app.playground.AbstractTest;
import com.nj.learning.elasticsearch.springboot.app.playground.sec05.entity.Garment;
import com.nj.learning.elasticsearch.springboot.app.playground.sec05.repository.GarmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import tools.jackson.core.type.TypeReference;

import java.util.List;

public class NativeAndCriteriaQueryTest  extends AbstractTest {

    @Autowired
    private GarmentRepository garmentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeAll
    public void dataSetup() {
        var garments = this.readResource("sec05/garments.json", new TypeReference<List<Garment>>() {

        });
        this.garmentRepository.saveAll(garments);
        Assertions.assertEquals(20, this.garmentRepository.count());
    }

    @Test
    public void criteriaQuery() {
        var nameIsShirt = Criteria.where("name").is("Shirt");
        this.verify(nameIsShirt, 1);

        var priceAbove100 = Criteria.where("price").greaterThan(100);
        this.verify(priceAbove100, 5);

        this.verify(nameIsShirt.or(priceAbove100), 6);

        var brandIsZara = Criteria.where("brand").is("Zara");
        this.verify(priceAbove100.and(brandIsZara.not()), 3);

        var fuzzyMatchShort = Criteria.where("name").fuzzy("short");
        this.verify(fuzzyMatchShort, 1);

        // We can boost
        // Criteria.where("brand").is("Zara").boost(3.0)

        // We can also do geo point
        // Criteria.where("location").within(point, distance)
    }

    @Test
    public void boolQuery() {

        var occasionCasual = Query.of(b -> b.term(
                TermQuery.of(tb -> tb.field("occasion").value("Casual"))
        ));
        var colorBrown = Query.of(b -> b.term(
                TermQuery.of(tb -> tb.field("color").value("Brown"))
        ));
        var priceBelow50 = Query.of(b -> b.range(
                RangeQuery.of(rb -> rb.number(
                        NumberRangeQuery.of(nrb -> nrb.field("price").lte(50d))
                ))
        ));

        var query = Query.of(b -> b.bool(
                BoolQuery.of(bb -> bb.filter(occasionCasual, priceBelow50).should(colorBrown))
        ));

        var nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .build();

        var searchHits = this.elasticsearchOperations.search(nativeQuery, Garment.class);
        searchHits.forEach(this.print());
        Assertions.assertEquals(4, searchHits.getTotalHits());

    }

    private void verify(Criteria criteria, int expectedResultCount) {
        var query = CriteriaQuery.builder(criteria).build();
        var searchHits = this.elasticsearchOperations.search(query, Garment.class);
        searchHits.forEach(this.print());
        Assertions.assertEquals(expectedResultCount, searchHits.getTotalHits());
    }
}
