package com.asadaclan.accounting.models;

import org.springframework.web.bind.annotation.BindParam;

public record TwilioInboundMessage(
        @BindParam("MessageSid") String messageSid,
        @BindParam("AccountSid") String accountSid,
        @BindParam("MessagingServiceSid") String messagingServiceSid,
        @BindParam("From") String from,
        @BindParam("To") String to,
        @BindParam("Body") String body,
        @BindParam("NumMedia") Integer numMedia,
        @BindParam("NumSegments") Integer numSegments,
        @BindParam("MediaContentType0") String mediaContentType0,
        @BindParam("MediaUrl0") String mediaUrl0) {
}
