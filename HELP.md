# Payment System
This 

The following was discovered as part of building this project:

* The original package name 'com.niroren.risk-engine' is invalid and this project uses 'com.niroren.riskengine' instead.

# Getting Started

* Start the zookeeper:
  > cd C:\kafka\kafka_2.13-2.4.0<br> 
  >.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties<br>
  
* Start Kafka broker:
  > .\bin\windows\kafka-server-start.bat .\config\server.properties<br>
                      
* Create payments topic:
  > .\bin\kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic payments

* In a separate terminal, start Confluent Schema Registry:
  > ./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties
### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/maven-plugin/)
* [JOOQ Access Layer](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-jooq)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-kafka)
* [Jersey](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-jersey)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)

