# Getting Started

How to manually start Kafka cluster:
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
* [Jersey](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-jersey)

