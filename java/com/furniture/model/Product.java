package com.furniture.model;

import java.sql.Timestamp;

public class Product {

    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private int stock;
    private String imageUrl;
    private String condition;
    private Timestamp createdAt; // ✅ New field

    // Constructors
    public Product() {}

    public Product(int id, String name, String description, double price,
                   String category, int stock, String imageUrl, String condition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.condition = condition;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public String getCondition() { return condition; }
    public Timestamp getCreatedAt() { return createdAt; } // ✅ New getter

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setStock(int stock) { this.stock = stock; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; } // ✅ New setter

    // Optional alias method so your servlet line remains unchanged:
    public void setProductCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name +
                ", price=" + price + ", category=" + category +
                ", stock=" + stock + ", condition=" + condition +
                ", createdAt=" + createdAt + "]";
    }
}
