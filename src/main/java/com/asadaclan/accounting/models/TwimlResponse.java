package com.asadaclan.accounting.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Response")
public record TwimlResponse(@JacksonXmlProperty(localName = "Message") String message) {
}
