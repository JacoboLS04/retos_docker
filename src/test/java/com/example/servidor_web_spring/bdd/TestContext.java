package com.example.servidor_web_spring.bdd;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

@Component
public class TestContext {
    private String currentUsername;
    private ResultActions resultActions;
    private boolean isAuthenticated = true;

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public ResultActions getResultActions() {
        return resultActions;
    }

    public void setResultActions(ResultActions resultActions) {
        this.resultActions = resultActions;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public void reset() {
        this.currentUsername = null;
        this.resultActions = null;
        this.isAuthenticated = true;
    }
}
