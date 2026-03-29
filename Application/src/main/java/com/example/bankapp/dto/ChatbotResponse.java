package com.example.bankapp.dto;

import java.util.Map;

public class ChatbotResponse {
    private String intent;
    private String message;
    private Map<String, Object> params;

    public ChatbotResponse() {}

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
}
