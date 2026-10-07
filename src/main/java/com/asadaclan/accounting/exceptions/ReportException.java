package com.asadaclan.accounting.exceptions;

public class ReportException extends Exception{
    public ReportException(){
        super();
    }
    public ReportException(String message){
        super(message);
    }
    public ReportException(Exception exception){
        super(exception);
    }
    
}
