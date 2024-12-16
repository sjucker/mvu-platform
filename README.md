# MVU Platform

## Development

* Start DB in Docker container:  
  `docker compose -p mvu -f src/main/docker/postgres.yml down && docker compose -p mvu -f src/main/docker/postgres.yml up --build`

* Generate the jOOQ-code by running the following command (make sure Docker is running):  
  `mvn clean test-compile -Djooq-codegen-skip=false`
  Or use the run configuration `generate jOOQ code`.

## Build

`mvn clean package -Pproduction`

## Updates

* Update Maven Parent
    * `mvn -U versions:display-parent-updates`
    * `mvn -U versions:update-parent`
* Update Versions in Properties
    * `mvn -U versions:display-property-updates`
    * `mvn -U versions:update-properties`

## Sonar

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sjucker_mvu-platform&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sjucker_mvu-platform)
