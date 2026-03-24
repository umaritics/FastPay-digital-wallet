package org.example.fastpay.utils;

import org.example.fastpay.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String accessToken; // To securely make API calls

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void loginUser(User user, String token) {
        this.currentUser = user;
        this.accessToken = token;
    }

    public void logout() {
        this.currentUser = null;
        this.accessToken = null;
    }

    public User getCurrentUser() { return currentUser; }
    public String getAccessToken() { return accessToken; }
}