#!/usr/bin/env bash

# Wait for database startup only defined
if [[ -z "${DB_HOST}" ]]; then
  echo "Skipping DB connection validation"
else
  echo "Waiting for MySQL database to be available..."
  /app/wait-for-it.sh -h $DB_HOST -p $DB_PORT -t 30
  echo "Verified MySQL DB aviailability."
fi

# Wait for Kafka Broker startup
echo "Waiting for Kafka Broker to be available..."
/app/wait-for-it.sh -h $KAFKA_BOOTSTRAP_SERVER_HOST -p $KAFKA_BOOTSTRAP_SERVER_PORT -t 30
echo "Verified Kafka Broker aviailability."

java -Dspring.profiles.active=docker -jar $SERVICE_JAR_FILE