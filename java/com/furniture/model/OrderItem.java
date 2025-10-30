package com.furniture.model;

public class OrderItem {
    private int id; // The primary key of the order_items table
    private int orderId; // Foreign key back to the order
    private int productId; // Foreign key to the product table
    private String productName; // Denormalized field from product table
    private int quantity;
    private double unitPrice;
    
    // Convenience field for JSP display (calculated)
    private double lineTotal; 

    // Constructor
    public OrderItem() {}

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    // Calculated field getter/setter (optional, but convenient)
    public double getLineTotal() {
        // Calculate on demand or use the stored field
        return this.quantity * this.unitPrice;
    }
    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }
}