package com.furniture.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String name;
    private String email;
    // Must be a Timestamp or Date object for JSTL fmt:formatDate to work
    private Timestamp createdAt; 

    // Constructor
    public User() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
