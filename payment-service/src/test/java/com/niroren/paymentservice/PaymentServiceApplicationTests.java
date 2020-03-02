package com.niroren.paymentservice;

import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class PaymentServiceApplicationTests {

	@ClassRule
	private EmbeddedKafkaRule embeddedKafkaBroker = new EmbeddedKafkaRule(1, true, "payments", "validated-payments");

	@Test
	void contextLoads() {
	}

}
