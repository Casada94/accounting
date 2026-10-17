package com.asadaclan.accounting.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Response")
public class TwimlResponse {
    private final String message;

    public TwimlResponse(String message) {
        this.message = message;
    }

    @JacksonXmlProperty(localName = "Message")
    public String message() {
        return message;
    }
}
