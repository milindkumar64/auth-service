package com.arth.auth.exception;

public class RoleNotFoundException extends RuntimeException{

    private String statusCode;
    private String message;
    public RoleNotFoundException(String statusCode, String message){
    super(message);
    this.statusCode = statusCode;
    this.message = message;
    }
}
