package com.asadaclan.accounting.producers;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.asadaclan.accounting.exceptions.CryptoException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class EmailProducer {

    private JavaMailSender mailSender; 
    private List<String> recipients;

    public EmailProducer(JavaMailSender mailSender, @Value("email.recepients") List<String> recipients)
            throws CryptoException {
        this.recipients = recipients;
    }

    public void sendMail(String subject, String bodyText, File file) throws MessagingException {
        // Create a MimeMessage
        MimeMessage message = mailSender.createMimeMessage();

        // Use MimeMessageHelper to configure the message. The 'true' argument indicates
        // a multipart message.
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(String.join(",", recipients));
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
