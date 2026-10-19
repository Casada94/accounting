package com.asadaclan.accounting.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asadaclan.accounting.models.TwilioInboundMessage;
import com.asadaclan.accounting.models.TwimlResponse;
import com.asadaclan.accounting.services.InboundMessageService;

@RestController
public class RcsController {

    private static final String RCS_PREFIX = "rcs:";

    private final InboundMessageService inboundMessageService;

    public RcsController(InboundMessageService inboundMessageService) {
        this.inboundMessageService = inboundMessageService;
    }

    @PostMapping(value = "/rcs", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public TwimlResponse receiveRcs(@ModelAttribute TwilioInboundMessage message) {
        String sender = normalizeSender(message.from());
        return new TwimlResponse(inboundMessageService.process(sender, message.body()));
    }

    private static String normalizeSender(String sender) {
        if (sender != null && sender.startsWith(RCS_PREFIX)) {
            return sender.substring(RCS_PREFIX.length());
        }
        return sender;
    }
}
