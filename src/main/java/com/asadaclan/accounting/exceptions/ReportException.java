package com.asadaclan.accounting.exceptions;

public class ReportException extends RuntimeException {
    public ReportException(){
        super();
    }
    public ReportException(String message){
        super(message);
    }
    public ReportException(Exception exception){
        super(exception);
    }

    public ReportException(String message, Exception exception) {
        super(message, exception);
    }
    
}
