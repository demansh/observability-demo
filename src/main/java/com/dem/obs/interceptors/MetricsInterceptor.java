package com.dem.obs.interceptors;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class MetricsInterceptor implements HandlerInterceptor {

    private final Histogram latencyHistogram;
    private final Counter rpsCounter;

    public MetricsInterceptor(CollectorRegistry registry) {
        latencyHistogram = Histogram.build()
                .name("api_latency_duration_ms")
                .help("Count api endpoints latency")
                .buckets(0.1, 0.5, 1, 5, 10, 50, 100, 200, 300, 500)
                .labelNames("path", "method", "status")
                .register(registry);
        rpsCounter = Counter.build()
                .name("api_rps_count")
                .help("Api calls counter")
                .labelNames("path", "method", "status")
                .register(registry);
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) throws Exception {
        long executeTime = System.currentTimeMillis() - (Long) request.getAttribute("startTime");
        latencyHistogram.labels(request.getServletPath(), request.getMethod(), String.valueOf(response.getStatus()))
                .observe(executeTime);
        rpsCounter.labels(request.getServletPath(), request.getMethod(), String.valueOf(response.getStatus())).inc();
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
