package com.orders.pojos;

public class LoginResponse {
  private final boolean loginResult;
  private final String sessionId;

  private final String status;

  public LoginResponse(boolean loginResult, String sessionId, String status) {
    this.loginResult = loginResult;
    this.sessionId = sessionId;
    this.status = status;
  }

  public boolean isLoginResult() {
    return loginResult;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getStatus() {
    return status;
  }
}

