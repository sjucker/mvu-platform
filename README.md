# MVU Platform

## Development

* Start DB in Docker container:  
  `docker compose -p mvu -f src/main/docker/postgres.yml down && docker compose -p mvu -f src/main/docker/postgres.yml up --build`

* Generate the jOOQ-code by running the following command (make sure Docker is running):  
  `mvn clean test-compile -Djooq-codegen-skip=false`
  Or use the run configuration `generate jOOQ code`.

## Build

* `mvn clean package -Pproduction`
* `mvn dependency:resolve-sources`

## Updates

* Update Maven Parent
    * `mvn -U versions:display-parent-updates`
    * `mvn -U versions:update-parent`
* Update Versions in Properties
    * `mvn -U versions:display-property-updates`
    * `mvn -U versions:update-properties`

## Sonar

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sjucker_mvu-platform&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sjucker_mvu-platform)

## Heroku

### Database

* `heroku pg:info --app mvu`

### Database Backup

* Prod:  
  `heroku pg:backups:capture --app mvu`  
  `heroku pg:backups:download --app mvu`

* Restore locally:
    * Drop all tables
    * `pg_restore --no-owner -h localhost -U mvu -d mvu -W latest.dump`

## Infrastructure

* Google Cloud Storage: https://console.cloud.google.com/storage/browser/mvurdorf-notenablage
* Heroku: https://dashboard.heroku.com/apps/mvu
