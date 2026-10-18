package com.asadaclan.accounting.producers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Address;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

class EmailProducerTests {

    @TempDir
    Path tempDir;

    @Test
    void sendsReportToTheRequestedRecipient() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        EmailProducer producer = new EmailProducer(mailSender);
        File attachment = Files.writeString(tempDir.resolve("report.pdf"), "report").toFile();

        producer.sendMail("owner@example.com", "Financial Report", "", attachment);

        assertThat(Arrays.stream(message.getAllRecipients()).map(Address::toString))
                .containsExactly("owner@example.com");
        verify(mailSender).send(message);
    }
}
