package com.asadaclan.accounting.controllers;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.UnregisteredPhoneNumberException;
import com.asadaclan.accounting.models.TwimlResponse;

@RestControllerAdvice(assignableTypes = {SmsController.class, RcsController.class})
public class SmsExceptionHandler {

    @ExceptionHandler(CommandParseException.class)
    public TwimlResponse handleCommandParseException(CommandParseException exception) {
        return new TwimlResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public TwimlResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return new TwimlResponse(exception.getMessage());
    }

    @ExceptionHandler(UnregisteredPhoneNumberException.class)
    public TwimlResponse handleUnregisteredPhoneNumberException(UnregisteredPhoneNumberException exception) {
        return new TwimlResponse(exception.getMessage());
    }
}
