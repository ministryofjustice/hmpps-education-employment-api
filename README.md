[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-education-employment-dev.hmpps.service.justice.gov.uk/swagger-ui.html)

# hmpps-education-employment-api

**Education & Employment Domain Microservice - resource server for offender work readiness data**

# Instructions


## Running locally

The service has no external service dependencies currently other than HMPPS Auth and Postgres. The service will in the fullness of time send SQS audit events but does not currentlty do so. 

To run the service locally with Docker, it is assumed that the developer will wish to use HMPPS Auth dev instance, Postgres in docker, for which docker compose can be used:

- to run this application independently e.g. in IntelliJ:

`docker-compose up --scale hmpps-education-employment-api=0`

- else to run the application in docker also:

`docker-compose up`

## Purpose

The API supports the [hmpps-education-employment-ui](https://github.com/ministryofjustice/hmpps-education-employment-ui), storing a collection of responses to questions provided by Prison Employment Leads (PEL), related to an offenders willingess and ability to seek and obtain employment on leaving prison.

Each offender dealt with by a PEL will have a work readiness profile created, the data in that profile stored by the API within Postgres/AWS RDS in a jsonb column.

### Architecture

Architecture and Technical Design docs can be found decision records start [here](https://dsdmoj.atlassian.net/wiki/spaces/ESWE/pages/3502571831/Architecture)