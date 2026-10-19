package com.asadaclan.accounting.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asadaclan.accounting.models.TwilioInboundMessage;
import com.asadaclan.accounting.models.TwimlResponse;
import com.asadaclan.accounting.services.InboundMessageService;

@RestController
public class SmsController {

    private final InboundMessageService inboundMessageService;

    public SmsController(InboundMessageService inboundMessageService) {
        this.inboundMessageService = inboundMessageService;
    }

    @PostMapping(value = "/sms", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public TwimlResponse receiveSms(@ModelAttribute TwilioInboundMessage message) {
        return new TwimlResponse(inboundMessageService.process(message.from(), message.body()));
    }
}
