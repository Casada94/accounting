package com.asadaclan.accounting.exceptions;

public class UnregisteredPhoneNumberException extends RuntimeException {
    public UnregisteredPhoneNumberException() {
        super("Phone number is not registered");
    }
}
