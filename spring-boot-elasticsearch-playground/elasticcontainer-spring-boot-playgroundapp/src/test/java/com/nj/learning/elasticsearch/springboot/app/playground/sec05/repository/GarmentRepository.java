package com.nj.learning.elasticsearch.springboot.app.playground.sec05.repository;

import com.nj.learning.elasticsearch.springboot.app.playground.sec05.entity.Garment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarmentRepository extends ElasticsearchRepository<Garment, String> {
}
