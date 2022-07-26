# hmpps-education-employment-api

**Education & Employment Domain Microservice to own offender work readiness data**

# Instructions


## Running locally

For running locally against docker instances of the following services:
- [hmpps-auth](https://github.com/ministryofjustice/hmpps-auth)
- run this application independently e.g. in IntelliJ

`docker-compose up --scale hmpps-education-employment-api=0`

## Running all services including this service

`docker-compose up`

## Running locally against T3 test services

This is straight-forward as authentication is delegated down to the calling services.  Environment variables to be set are as follows:-
```
API_BASE_URL_OAUTH=https://sign-in-dev.hmpps.service.justice.gov.uk/auth
API_BASE_URL_PRISON=https://api-dev.prison.service.justice.gov.uk
```


### Architecture

Architecture decision records start [here](doc/architecture/decisions/0001-use-adr.md)