package com.dem.obs.config;

import com.dem.obs.interceptors.MetricsInterceptor;
import com.dem.obs.queue.MessageQueueService;
import com.dem.obs.queue.QueueSizeMeter;
import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private MetricsInterceptor metricsInterceptor;

    @Bean
    public MetricsInterceptor metricsInterceptor(CollectorRegistry collectorRegistry) {
        MetricsInterceptor metricsInterceptor = new MetricsInterceptor(collectorRegistry);
        this.metricsInterceptor = metricsInterceptor;
        return metricsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/**");
    }

    @Bean
    public MessageQueueService getMessageQueueService() {
        return new MessageQueueService();
    }

    @Bean
    public QueueSizeMeter queueSizeMeter(CollectorRegistry collectorRegistry) {
        return new QueueSizeMeter(collectorRegistry, getMessageQueueService());
    }
}
