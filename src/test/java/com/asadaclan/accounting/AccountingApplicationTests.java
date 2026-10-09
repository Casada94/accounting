package com.asadaclan.accounting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.asadaclan.accounting.producers.EmailProducer;

@SpringBootTest
class AccountingApplicationTests {

	@MockitoBean
	private EmailProducer emailProducer;

	@Test
	void contextLoads() {
	}

}
