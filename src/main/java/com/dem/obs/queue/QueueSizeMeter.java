package com.dem.obs.queue;

import com.dem.obs.api.MessageAPI;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

public class QueueSizeMeter {
    private final Logger log = LogManager.getLogger(MessageAPI.class);
    private final MessageQueueService messageQueueService;
    private final Gauge queueSizeGauge;

    public QueueSizeMeter(
            CollectorRegistry registry,
            MessageQueueService messageQueueService) {
        this.messageQueueService = messageQueueService;
        queueSizeGauge = Gauge.build()
                .name("queue_size")
                .help("Queue size")
                .create()
                .register(registry);
    }

    @Scheduled(fixedDelay = 5000)
    public void measureQueueSize() {
        try {
            queueSizeGauge.set(messageQueueService.size());
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
        }
    }
}
