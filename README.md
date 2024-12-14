# MVU Platform

## Development

* Start DB in Docker container:  
  `docker compose -p mvu -f src/main/docker/postgres.yml down && docker compose -p mvu -f src/main/docker/postgres.yml up --build`

* Generate the jOOQ-code by running the following command (make sure Docker is running):  
  `./mvnw clean test-compile -Djooq-codegen-skip=false`
  Or use the run configuration `generate jOOQ code`.
