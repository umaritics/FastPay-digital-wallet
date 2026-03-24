package org.example.fastpay.models;

public class User {
    private String id;
    private String email;
    private String role;
    private String fullName;
    private String phone;

    public User(String id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }

    // Setters (Used later when Dashboard fetches the profile)
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
}