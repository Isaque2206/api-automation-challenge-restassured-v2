package com.isaque.qa.model;

public class LoginResponse {
    public String message;
    public String authorization;

    public String getToken() {
        return authorization;
    }
}
