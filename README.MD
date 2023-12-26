# Run
1. Run docker with prometheus and grafana:
```
cd monitoring
docker-compose up
```
2. Run server
```
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```
3. Run test to load API
```
./mvnw test -Dtest="LoadTest#test"
```

# Links:
1. App: http://localhost:8080/
2. Actuator: http://localhost:8080/actuator/prometheus
3. Prometheus: http://localhost:9090/
4. Grafana: http://localhost:3000/

# Grafana hints:
RPS:
```
sum(irate(api_rps_count_total[$__rate_interval])) by (method, path, status)
```
Latency
```
histogram_quantile(0.99, sum(rate(api_latency_duration_ms_bucket[$__rate_interval])) by (le, path, method, status))
```
Queue size
```
queue_size
```
