package com.asadaclan.accounting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.asadaclan.accounting.producers.EmailProducer;
import com.asadaclan.accounting.repositories.ExpenseRepository;
import com.asadaclan.accounting.repositories.PayoutRepository;
import com.asadaclan.accounting.repositories.ReportRepository;
import com.asadaclan.accounting.repositories.TransactionRepository;
import com.asadaclan.accounting.repositories.UndoRepository;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
		+ "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
		+ "org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration,"
		+ "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
		+ "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration")
class AccountingApplicationTests {
	@MockitoBean
	private EmailProducer emailProducer;

	@MockitoBean
	private TransactionRepository transactionRepository;

	@MockitoBean
	private PayoutRepository payoutRepository;

	@MockitoBean
	private ExpenseRepository expenseRepository;

	@MockitoBean
	private ReportRepository reportRepository;

	@MockitoBean
	private UndoRepository undoRepository;

	@Test
	void contextLoads() {
	}

}
