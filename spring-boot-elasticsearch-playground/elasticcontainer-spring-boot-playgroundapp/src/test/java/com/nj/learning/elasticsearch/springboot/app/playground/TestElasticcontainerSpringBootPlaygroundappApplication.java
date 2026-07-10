package com.nj.learning.elasticsearch.springboot.app.playground;

import org.springframework.boot.SpringApplication;

public class TestElasticcontainerSpringBootPlaygroundappApplication {

    public static void main(String[] args) {
        SpringApplication.from(ElasticcontainerSpringBootPlaygroundappApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
