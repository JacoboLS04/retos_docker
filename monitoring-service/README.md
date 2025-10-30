Monitoring Service (Go)

This small service allows registering microservices to be monitored by Prometheus/Blackbox.

Features:
- POST /register {"name":"svc","url":"http://svc:8080/health"}
- GET /services
- /metrics Prometheus metrics (service_up, service_last_check_timestamp)
- POST /alert Alertmanager webhook receiver (forwards to notifications service)

Docker:
- writes file_sd targets to `/out/targets.json` (mount this to Prometheus `/etc/prometheus/file_sd`).

Env:
- PORT (default 8085)
- TARGETS_FILE (default /out/targets.json)
- NOTIFICATIONS_ENDPOINT (optional) to forward Alertmanager webhooks
