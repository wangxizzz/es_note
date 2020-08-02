package com.example.highlevelclient.service;

import com.example.highlevelclient.BaseTest;
import com.example.highlevelclient.entity.Crowd;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author wangxi created on 2020/8/2 4:39 PM
 * @version v1.0
 */
public class CrowdServiceTest extends BaseTest {

    @Resource
    private CrowdService crowdService;

    public long getRandomId(Random random) {
        return (long) random.nextInt(1000000000);
    }

    @Test
    public void batchInsertTest() throws Exception {
        AtomicInteger j = new AtomicInteger(0);
        Random random = new Random();
        for (int count = 0; count < 10; count++) {
            CompletableFuture.runAsync(() -> {
                Set<Crowd> crowdSet = new HashSet<>();
                for (int i = 0; i < 2000; i++) {
                    Crowd crowd = Crowd.builder()
                            .crowdId(getRandomId(random))
                            .userId(getRandomId(random))
                            .insertTime(new Date())
                            .build();
                    crowdSet.add(crowd);
                }
                boolean res = false;
                try {
                    res = crowdService.batchInsert(1, crowdSet);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("=============== res = " + res + " j = " + j.getAndIncrement());
            });

            Thread.currentThread().join();
        }
    }
}