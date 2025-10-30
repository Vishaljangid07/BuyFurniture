package com.furniture.model;

/**
 * Model class representing a single item line in the shopping cart.
 * Wraps a Product object and holds the quantity ordered.
 */
public class CartItem {
    private Product product;
    private int quantity;

    // Constructor
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // --- Methods REQUIRED BY OrderDAO ---

    // Returns the unit price for the database insert
    public double getPrice() {
        return this.product.getPrice();
    }
    
    // Returns the total price for JSP display/total calculation
    public double getTotalPrice() {
        return this.product.getPrice() * this.quantity;
    }

    // Returns the product ID for the order_items table
    public int getProductId() {
        return this.product.getId();
    }
    
    // Returns the product name for the order_items table
    public String getName() {
        return this.product.getName();
    }
}
