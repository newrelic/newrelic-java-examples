# Dockerized Springboot Petclinic Service With New Relic Java Agent

Dockerized version of the [SpringBoot PetClinic service](https://github.com/spring-projects/spring-petclinic) with the [New Relic Java Agent](https://docs.newrelic.com/docs/apm/agents/java-agent/getting-started/introduction-new-relic-java/).

## Configure the Java agent

Before running the container you must modify the following environment variables in the Dockerfile to configure where the Java agent reports.

These two define a unique entity that is associated with a specific APM account and New Relic environment:
* Set the APM entity name: `ENV NEW_RELIC_APP_NAME=JavaPetClinic`
* License key for account: `ENV NEW_RELIC_LICENSE_KEY='<license_key>'`

Set the following based on which New Relic environment the APM account is associated with:
* US Production
    ```
    ENV NEW_RELIC_HOST=collector.newrelic.com
    ENV NEW_RELIC_API_HOST=rpm.newrelic.com
    ENV NEW_RELIC_METRIC_INGEST_URI=https://metric-api.newrelic.com/metric/v1
    ENV NEW_RELIC_EVENT_INGEST_URI=https://insights-collector.newrelic.com/v1/accounts/events
    ```
* EU Production
    ```
    ENV NEW_RELIC_HOST=collector.eu01.nr-data.net
    ENV NEW_RELIC_API_HOST=api.eu.newrelic.com
    ENV NEW_RELIC_METRIC_INGEST_URI=https://metric-api.eu.newrelic.com/metric/v1
    ENV NEW_RELIC_EVENT_INGEST_URI=https://insights-collector.eu01.nr-data.net/v1/accounts/events
    ```
* US Staging
    ```
    ENV NEW_RELIC_HOST=staging-collector.newrelic.com
    ENV NEW_RELIC_API_HOST=staging.newrelic.com
    ENV NEW_RELIC_METRIC_INGEST_URI=https://staging-metric-api.newrelic.com/metric/v1
    ENV NEW_RELIC_EVENT_INGEST_URI=https://staging-insights-collector.newrelic.com/v1/accounts/events
    ```

(OPTIONAL) Enable JFR monitoring for enhanced JVM details:
* `ENV NEW_RELIC_JFR_ENABLED=true`

See [Java agent configuration](https://docs.newrelic.com/docs/apm/agents/java-agent/configuration/java-agent-configuration-config-file/) for a complete list of config options.

## Building/Running Dockerized Petclinic Service

### Option 1: Docker Compose

Build and run:
`docker-compose up -d`

Stop:
`docker-compose down`

### Option 2: Docker Build/Run

Build Docker Image:
`docker build --tag petclinic-app .`

Run Docker Container:
`docker run -p 8080:8080 petclinic-app`

Stop Docker Container:
`docker ps`
`docker stop <CONTAINER ID>`

## Make a Request to the Petclinic Service

By default, the Petclinic Service will be accessible at: http://localhost:8080

Example `curl` request:  
`curl --request GET --url http://localhost:8080/vets --header 'content-type: application/json'`
