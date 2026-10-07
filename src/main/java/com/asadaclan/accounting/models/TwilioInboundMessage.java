package com.asadaclan.accounting.models;

import lombok.Data;

@Data
public class TwilioInboundMessage {
    private final String messageSid;
    private final String accountSid;
    private final String messagingServiceSid;
    private final String from;
    private final String to;
    private final String body;
    private final int numMedia;
    private final int numSegments;
    private final String mediaContentType0;
    private final String mediaUrl0;
    
}
