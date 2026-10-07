package com.asadaclan.accounting.exceptions;

public class CryptoException extends Exception {
    private String message;

    public CryptoException(String message){
        this.message = message;
    }
    public CryptoException(Exception e){
        this.message = e.getMessage();
    }

    /** GETTERS **/
    public String getMessage(){
        return message;
    }

    /** SETTERS **/
    public void setMessage(String message){
        this.message = message;
    }
}
