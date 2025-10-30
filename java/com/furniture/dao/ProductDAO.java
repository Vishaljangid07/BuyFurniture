package com.furniture.dao;

import com.furniture.controller.DBConnection;
import com.furniture.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    // Helper method to map ResultSet to Product object (CRITICAL FOR FIXING ERROR)
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        // Use no-arg constructor and setters (the safe approach)
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setCategory(rs.getString("category"));
        
        // FIX: Retrieve Stock (Field used for new table view)
        product.setStock(rs.getInt("stock"));
        
        product.setImageUrl(rs.getString("image_url"));
        product.setCondition(rs.getString("product_condition")); 
        
        // FIX: Retrieve CreatedAt (Field used in your JSP's new stock view)
        product.setCreatedAt(rs.getTimestamp("created_at"));
        
        return product;
    }

    // --- READ methods ---
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // FIX SQL: Explicitly selecting all columns ensures proper mapping
        String sql = "SELECT id, name, description, price, category, stock, image_url, product_condition, created_at FROM products";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // FIX: Use the mapping helper, resolving the constructor error
                products.add(mapResultSetToProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace(); // This is why the error was silent!
        }
        return products;
    }

    public Optional<Product> getProductById(int id) {
        // FIX SQL: Explicitly selecting all columns
        String sql = "SELECT id, name, description, price, category, stock, image_url, product_condition, created_at FROM products WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // FIX: Use the mapping helper, resolving the constructor error
                return Optional.of(mapResultSetToProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // --- CREATE method (addProduct) ---
    public boolean addProduct(Product product) {
        // FIX SQL: Added 'stock' field to INSERT
        String sql = "INSERT INTO products (name, description, price, category, stock, image_url, product_condition) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getCategory());
            
            // FIX: Set stock parameter
            ps.setInt(5, product.getStock()); 
            
            ps.setString(6, product.getImageUrl());
            ps.setString(7, product.getCondition()); 

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- DELETE method ---
    public boolean deleteProductById(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- UPDATE method ---
    public boolean updateProduct(Product product) {
        // FIX SQL: Added 'stock = ?' to the UPDATE
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, category = ?, stock = ?, image_url = ?, product_condition = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getCategory());
            
            // FIX: Set stock parameter
            ps.setInt(5, product.getStock()); 
            
            ps.setString(6, product.getImageUrl());
            ps.setString(7, product.getCondition());
            ps.setInt(8, product.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
 // Inside com.furniture.dao.ProductDAO.java

    /**
     * Deducts the specified quantity from the stock of a product within an existing transaction.
     * @param conn The active database connection (for transaction).
     * @param productId The ID of the product to update.
     * @param quantity The amount to deduct (usually cart quantity).
     * @return True if the stock was successfully updated (meaning sufficient stock existed), false otherwise.
     * @throws SQLException if a database error occurs.
     */
    public boolean updateStock(Connection conn, int productId, int quantity) throws SQLException {
        
       
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            
        
            ps.setInt(1, quantity);
            
         
            ps.setInt(2, productId);
            
           
            ps.setInt(3, quantity); 
            
            int rowsAffected = ps.executeUpdate();
           
            return rowsAffected > 0;
            
        }
    }
    
   
}