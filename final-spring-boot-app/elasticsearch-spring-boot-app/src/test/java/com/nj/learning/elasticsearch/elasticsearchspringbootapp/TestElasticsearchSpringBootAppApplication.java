package com.nj.learning.elasticsearch.elasticsearchspringbootapp;

import org.springframework.boot.SpringApplication;

public class TestElasticsearchSpringBootAppApplication {

    public static void main(String[] args) {
        SpringApplication.from(ElasticsearchSpringBootAppApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
