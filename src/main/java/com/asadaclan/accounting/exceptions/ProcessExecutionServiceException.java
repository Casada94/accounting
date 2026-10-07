package com.asadaclan.accounting.exceptions;

public class ProcessExecutionServiceException extends Exception {
    public ProcessExecutionServiceException(String message) {
        super(message);
    }

    public ProcessExecutionServiceException(Exception exception) {
        super(exception);
    }
    
    public ProcessExecutionServiceException(String message, Exception exception) {
        super(message, exception);
    }

}
