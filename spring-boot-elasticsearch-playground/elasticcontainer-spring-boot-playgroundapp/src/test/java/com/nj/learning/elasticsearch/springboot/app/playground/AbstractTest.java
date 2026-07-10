package com.nj.learning.elasticsearch.springboot.app.playground;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    protected <T> T readResource(String path, TypeReference<T> typeReference) {
        try {
            var classpath = "classpath:" + path;
            var file = this.resourceLoader.getResource(classpath).getFile();
            return this.objectMapper.readValue(file, typeReference);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected <T>Consumer<T> print() {
        return t -> log.info("{}", t);
    }
}
