package com.asadaclan.accounting.exceptions;

public class FileUtilException extends Exception{
    public FileUtilException(){
        super();
    }
    public FileUtilException(String message){
        super(message);
    }
    public FileUtilException(Exception exception){
        super(exception);
    }
    
}
