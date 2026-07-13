# hmpps-education-employment-api

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-education-employment-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-education-employment-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-education-employment-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://education-employment-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)
[![Pipeline [test -> build -> deploy]](https://github.com/ministryofjustice/hmpps-education-employment-api/actions/workflows/pipeline.yml/badge.svg?branch=main)](https://github.com/ministryofjustice/hmpps-education-employment-api/actions/workflows/pipeline.yml)
                                                                                  
# About 
**Education & Employment Domain Microservice - resource server for offender work readiness data**

# Instructions

## Running the application locally
This backend application depends on several services to run.

| Dependency    | Description                                              | Default                              | Override Env Var                                                                  |
|---------------|----------------------------------------------------------|--------------------------------------|-----------------------------------------------------------------------------------|
| hmpps-auth    | OAuth2 API server for authenticating requests            |                                      | `API_BASE_URL_OAUTH`                                                              |
| Database      | Database server (`postgres` on local, `RDS` on live env) |                                      | `DATABASE_NAME`, `DATABASE_ENDPOINT`, `DATABASE_USERNAME` and `DATABASE_PASSWORD` |


---
### Running with docker compose
The easiest way to run the app is to use docker compose to create the service and all dependencies.
1. Run
   ```shell
   docker compose --profile api up
   ```
   will run the application (from latest image) and PostgreSQL within a local docker instance.
2. Check if application is up and running
    * See `http://localhost:8080/health` to check the app is running.
    * See `http://localhost:8080/swagger-ui/index.html` to explore the OpenAPI spec document.
    * See `http://localhost:8080/info` to check the app info

---
### Running the application in IntelliJ
1. Run this
    ```shell
   docker compose up -d 
    ```
    * will start dependencies only without the API application
    * `-d` for detached run
2. Run `bootRun` with `local` profile group
    * either IntelliJ
        - run `bootRun`
        - with this env var: `spring.profiles.active=local`
    * or Gradle wrapper
      ```shell
      SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
      ```
      or
      ```shell
      ./gradlew bootRun --args='--spring.profiles.active=local'
      ```    

## Run docker image on local

### Build a local docker image
1. Build the app jar
2. Copy jar to project root
3. Build docker image

```shell
BUILD_NUMBER=1_0_0 ./gradlew clean assemble && cp ./build/libs/*.jar .
```
```shell
BUILD_NUMBER=1_0_0 docker build --build-arg BUILD_NUMBER=$BUILD_NUMBER . -t "hmpps-education-employment-api:local"
```
### Run a local docker image
* In `.env.docker.local`
```dotenv
SPRING_PROFILES_ACTIVE=developer
PRODUCT_ID=DPS034
# `host.docker.internal` (instead of `localhost`) for connecting the image to local DB of host 
DATABASE_ENDPOINT=host.docker.internal:5432
HMPPS_SAR_ADDITIONALACCESSROLE=WORK_READINESS_VIEW
```

then run this
```shell
docker run --name hmpps-education-employment-api-app --env-file .env.docker.local -p 8080:8080 -d "hmpps-education-employment-api:local"
```

## Purpose

The API supports the [hmpps-education-employment-ui](https://github.com/ministryofjustice/hmpps-education-employment-ui), storing a collection of responses to questions provided by Prison Employment Leads (PEL), related to an offenders willingess and ability to seek and obtain employment on leaving prison.

Each offender dealt with by a PEL will have a work readiness profile created, the data in that profile stored by the API within Postgres/AWS RDS in a jsonb column.

### Architecture

Architecture and Technical Design docs can be found decision records start [here](https://dsdmoj.atlassian.net/wiki/spaces/ESWE/pages/3502571831/Architecture)

### JSON Schema

There is a JSON schema in src/main/resources which describes the structure of the profile JSON that the client is expected to store and will subsequently get back. The schema is not currently used by the MVP - instead the JSON is serialized/deserialized on its way in and out of the API using a series of data classes in package:

- uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile

These classes have been built to represent the schema structure. The only validation performed currently is that the multi choice field values must conform to the enums with the data classes.
