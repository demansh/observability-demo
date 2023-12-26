# Set up
1. Open a terminal and execute:
```
cd monitoring
docker-compose up
```
2. Open another terminal and execute:
```
./mvnw clean install -DskipTests
```
3. Go to IDEA and run the server.
4. Open links to ensure all is running:
- App: http://localhost:8080/
- Actuator: http://localhost:8080/actuator/prometheus
- Prometheus: http://localhost:9090/
- Grafana: http://localhost:3000/

5. Generate some load:
- Go to `test/java/com.dem.obs/LoadTest.java`
- Run `test()`
- Edit `MSG_LENGTH` to increase/reduce amount of data per request

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
