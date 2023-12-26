package com.dem.obs.api;

import com.dem.obs.entities.Message;
import com.dem.obs.entities.MessageRequest;
import com.dem.obs.queue.MessageQueueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class MessageAPI {
    Logger log = LogManager.getLogger(MessageAPI.class);

    private final MessageQueueService messageQueue;

    @Autowired
    public MessageAPI(MessageQueueService messageQueue) {
        this.messageQueue = messageQueue;
    }

    @GetMapping("/")
    public ResponseEntity<Message> getMessage() {
        try {
            return ResponseEntity.ok(messageQueue.pop());
        } catch (RuntimeException e) {
            log.warn("Pop message failed", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<String> addMessage(@RequestBody MessageRequest message) {
        try {
            String msgId = UUID.randomUUID().toString();
            messageQueue.add(new Message(msgId, message.text()));
            return ResponseEntity.ok(msgId);
        } catch (RuntimeException e) {
            log.warn("Save message failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
