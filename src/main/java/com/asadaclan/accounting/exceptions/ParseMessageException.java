package com.asadaclan.accounting.exceptions;

public class ParseMessageException extends Exception{
    public ParseMessageException(){
        super();
    }
    public ParseMessageException(String message){
        super(message);
    }
    public ParseMessageException(Exception exception){
        super(exception);
    }
    
}
