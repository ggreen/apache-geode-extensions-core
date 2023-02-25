package io.spring.gemfire.perftest.components.runner;

import jakarta.annotation.Resource;
import nyla.solutions.core.patterns.creational.generator.MapTextCreator;
import org.apache.geode.cache.Region;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "action", havingValue = "putAllString")
public class PutAllStringPerfRunner implements  Runnable{

    private final int keyPadLength;
    private final int batchSize;
    private final int valueLength;
    private final String seedText;

    @Resource(name = "perfTestRegion")
    private Region<String, String> region;

    private Map<String, String> map;

    public PutAllStringPerfRunner(int batchSize, int keyPadLength, int valueLength, String seedText) {

        this.batchSize = batchSize;
        this.keyPadLength = keyPadLength;
        this.valueLength = valueLength;
        this.seedText = seedText;

    }

    public void setRegion(Region<String, String> region) {
        this.region = region;
    }


    void init() {
        map = MapTextCreator.builder().size(batchSize)
                .keyPadLength(keyPadLength)
                .valueLength(valueLength)
                .seedText(seedText)
                .build().create();
    }

    public void run() {
        region.putAll(map);
    }
}