# OVERVIEW
## ECS Service Discovery
The `discover-ecs` container runs the `discoverecs.py` to discover the running ECS Service instances and generates the 
prometheus scrape target configuration in the prometheus sd format. A sample prometheus sd configuration might look like

```
python discoverecs.py --directory /app/ecs-sd-files
```

A bind mount for `/app/ecs-sd-files` needs to be provided at the time of launching the `discover-ecs` 
container

## Prometheus with service discovery configuration
The `prometheus-ecs` container starts prometheus with service discovery configured.
```
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'ecs-sd-15s'
    scrape_interval: 15s
    file_sd_configs:
      - files:
        - /etc/prometheus/ecs-sd-files/15s-tasks.json
```

A bind mount for `/etc/prometheus/ecs-sd-files` needs to be provided at the time of launching the `/etc/prometheus` 
container
 