# Nitro Wizard

Spring Boot service that tells you which Wizard World elixirs can be brewed from a set of available ingredients.

## Features
- REST API for matching and listing elixirs.
- OpenAPI/Swagger UI for discovery.
- JSON responses with validation and clear errors.
- Configurable Wizard World API endpoints, timeouts, and synonyms via `application.yml`.
- Prometheus metrics + Grafana dashboard demo.

## Requirements
- Java 21
- Maven 3.8+

## Install
```
mvn -q test
```

## Run the Server
```
mvn -q spring-boot:run
```

Build and run the JAR:
```
mvn -q -DskipTests package
java -jar target/nitro-wizard.jar
```

OpenAPI docs:
- `http://localhost:8080/api-docs`
- `http://localhost:8080/swagger-ui.html`

## Metrics + Grafana Demo
Expose metrics at `http://localhost:8080/actuator/prometheus`.

Run Prometheus + Grafana with Docker:
```
sudo docker run -d --name prometheus -p 9090:9090 \
  --add-host=host.docker.internal:host-gateway \
  -v $(pwd)/infra/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro \
  prom/prometheus:v2.53.1

sudo docker run -d --name grafana -p 3000:3000 \
  --add-host=host.docker.internal:host-gateway \
  -e GF_SECURITY_ADMIN_USER=admin \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  -v $(pwd)/infra/grafana/provisioning:/etc/grafana/provisioning:ro \
  -v $(pwd)/infra/grafana/dashboards:/var/lib/grafana/dashboards:ro \
  grafana/grafana:11.1.0
```

Open Grafana: `http://localhost:3000` (admin/admin).  
Prometheus: `http://localhost:9090`.

If Prometheus shows the target as DOWN, the config includes both
`host.docker.internal:8080` and `172.17.0.1:8080` for Linux compatibility.

Custom counters:
- `nitro_wizard_elixir_match_requests_total`
- `nitro_wizard_elixir_list_requests_total`
- `nitro_wizard_elixir_sample_requests_total`
- `nitro_wizard_ping_requests_total`

Cleanup:
```
docker rm -f prometheus grafana
```
## Bruno Collection
Import the collection from `bruno/` (use the `local` environment).

## Postman Collection
Import `postman/Nitro Wizard API.postman_collection.json`.

## Screenshots
Bruno collection:

![Bruno Collection](docs/Screenshot%20from%202026-04-11%2016-54-02.png)

Grafana dashboard:

![Grafana Dashboard](docs/Screenshot%20from%202026-04-11%2017-24-44.png)

## REST API Examples
Match elixirs:
```
curl -s -X POST http://localhost:8080/api/elixirs/match \\
  -H 'Content-Type: application/json' \\
  -d '{"ingredients":["Boomslang Skin","Leech Juice"]}'
```
Note: `/api/elixirs/match` is POST-only.

Sample elixir:
```
curl -s http://localhost:8080/api/elixirs/sample
```

List elixirs:
```
curl -s http://localhost:8080/api/elixirs
```

List elixirs (full details):
```
curl -s "http://localhost:8080/api/elixirs?full=true"
```

Health check:
```
curl -s http://localhost:8080/api/health/ping
```

## Configuration
Runtime settings live in `src/main/resources/application.yml` under `wizard.api`.

Synonym mapping (optional) can be configured under `wizard.synonyms.map`:
```
wizard:
  synonyms:
    map:
      neemuoil: neem oil
      lacewing flies: lacewing flies
```

## Testing
```
mvn -q test
```

## Notes
- The Wizard World API may add fields over time. The client ignores unknown fields.
- If no elixirs are found for the provided ingredients, the API returns an empty list.

## AI Usage
This project may include AI-assisted development. Be ready to discuss how tools were used if asked.
