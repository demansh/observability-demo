package com.dem.obs.queue;

import com.dem.obs.entities.Message;
import com.dem.obs.exceptions.NotFoundException;
import com.dem.obs.exceptions.QueueException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MessageQueueService {
    private final FileQueue queue = new FileQueue();

    public void add(Message message) {
        try {
            queue.add(message);
        } catch (IOException | TimeoutException e) {
            throw new QueueException(e);
        }
    }

    public Message pop() {
        try {
            Message msg = queue.pop();
            if (msg == null) {
                throw new NotFoundException();
            }
            return msg;
        } catch (IOException | TimeoutException e) {
            throw new QueueException(e);
        }
    }

    public Long size() {
        try {
            return queue.size();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
