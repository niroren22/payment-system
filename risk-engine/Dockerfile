
FROM maven:3.6.3-jdk-11-openj9
MAINTAINER niroren22@gmail.com

ENV DB_HOST=localhost
ENV DB_PORT=3306

#VOLUME /risk-engine
RUN mkdir -p /usr/payments/risk-engine
COPY ../pom.xml /usr/payments/pom.xml
COPY pom.xml /usr/payments/risk-engine/pom.xml
COPY src /usr/payments/risk-engine/src

WORKDIR /usr/payments/risk-engine

RUN mvn clean install

ADD target/risk-engine-1.0-SNAPSHOT.jar /risk-engine-1.0.jar
ADD ../wait-for-it.sh /wait-for-it.sh
EXPOSE 8082
ENTRYPOINT ["/wait-for-it.sh","-h","${DB_HOST}","-p","${DB_PORT}","-t","30","--","java","-jar","risk-engine-1.0.jar"]