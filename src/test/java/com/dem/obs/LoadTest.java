package com.dem.obs;

import com.dem.obs.entities.Message;
import com.dem.obs.entities.MessageRequest;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class LoadTest {
    private static final String URL = "http://localhost:8080/";
    private static final Long REQS_NUMBER = 1000L;
    private static final int MSG_LENGTH = 10;

    @Test
    public void test() throws InterruptedException {
        ForkJoinPool customThreadPool = new ForkJoinPool(10);
        while (true) {
            System.out.println("test is running");
            customThreadPool.submit(() ->
                    generateRequests(REQS_NUMBER, MSG_LENGTH).stream().parallel().forEach(Runnable::run)
            );
            customThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    private Collection<Runnable> generateRequests(long number, int length) {
        Collection<Runnable> tasks = new HashSet<>();
        for (int i = 0; i < number; i++) {
            tasks.add(new Random().nextBoolean() ? postTask(length) : getTask());
        }
        return tasks;
    }

    private Runnable postTask(int length) {
        return () -> {
            try {
                post(length);
            } catch (RuntimeException e) {
                //ingore
            }
        };
    }

    private Runnable getTask() {
        return () -> {
            try {
                get();
            } catch (RuntimeException e) {
                //ingore
            }
        };
    }

    private void post(int length) {
        RestTemplate template = new RestTemplate();
        template.postForObject(
                URL,
                new MessageRequest(RandomString.make(length)),
                String.class
        );
    }

    private void get() {
        RestTemplate template = new RestTemplate();
        template.getForObject(
                URL,
                Message.class
        );
    }

    @Test
    public void postTest() {
        postTask(10).run();
    }

    @Test
    public void getTest() {
        getTask().run();
    }
}
