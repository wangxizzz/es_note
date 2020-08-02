package com.example.highlevelclient.service;

import com.example.highlevelclient.BaseTest;
import com.example.highlevelclient.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
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
 * @author wangxi created on 2020/8/2 4:03 PM
 * @version v1.0
 */
public class UserServiceTest extends BaseTest {

    @Resource
    private UserService userService;

    public static String[] username = {"二珂", "提莫", "纳豆", "王喜", "wangxi", "xiaoming", "zhangsan", "wangwu"};
    public static String[] userSex = {"sex001", "sex002"};

    public long getRandomId(Random random) {
        return (long) random.nextInt(1000000000);
    }

    @Test
    public void createIndexTest() {
        userService.createIndex();
    }

    @Test
    public void batchInsert() throws IOException, InterruptedException {
        AtomicInteger j = new AtomicInteger(0);
        for (int count = 0; count < 10; count++) {
            CompletableFuture.runAsync(() -> {
                Set<User> userSet = new HashSet<>();
                Random random = new Random();
                for (int i = 0; i < 2000; i++) {
                    User user = User.builder()
                            .userId(getRandomId(random))
                            .username(username[random.nextInt(username.length) % username.length])
                            .city("city" + random.nextInt(32))
                            .userSex(userSex[i % userSex.length])
                            .userAge(random.nextInt(100))
                            .birthday(new Date())
                            .crowdIds(Lists.newArrayList(getRandomId(random), getRandomId(random), getRandomId(random)))
                            .build();
                    userSet.add(user);
                }
                boolean res = false;
                try {
                    res = userService.batchInsert(1, userSet);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("===============res = " + res + "j = " + j.getAndIncrement());
            });
        }
        Thread.currentThread().join();
    }

    @Test
    public void groupByCountTest() throws IOException {
        userService.groupByCount();
    }
}