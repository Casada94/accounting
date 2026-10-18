package com.asadaclan.accounting.producers;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailProducer {

    private final JavaMailSender mailSender;

    public EmailProducer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String recipient, String subject, String bodyText, File file) throws MessagingException {
        // Create a MimeMessage
        MimeMessage message = mailSender.createMimeMessage();

        // Use MimeMessageHelper to configure the message. The 'true' argument indicates
        // a multipart message.
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(recipient);
        helper.setSubject(subject);
        // The 'false' argument here indicates the text is plain text, not HTML
        helper.setText(bodyText, false);

        // Add the attachment
        FileSystemResource fileResource = new FileSystemResource(file.getPath());
        helper.addAttachment(fileResource.getFilename(), file); // You can specify a custom name for the attachment in
                                                                // the first argument

        // Send the message
        mailSender.send(message);
    }

}
