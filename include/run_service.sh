#!/usr/bin/env bash

if [[ -z "${DB_HOST}" ]]; then
  echo "Waiting for MySQL database to be available..."
  /app/wait-for-it.sh $DB_HOST:$DB_PORT -t 30
  echo "Verified MySQL DB aviailability."
fi

echo "Waiting for Kafka Broker to be available..."
/app/wait-for-it.sh -h $KAFKA_BOOTSTRAP_SERVER_HOST -p $KAFKA_BOOTSTRAP_SERVER_PORT -t 30
echo "Verified Kafka Broker aviailability."

java -Dspring.profiles.active=docker -jar $SERVICE_JAR_FILE