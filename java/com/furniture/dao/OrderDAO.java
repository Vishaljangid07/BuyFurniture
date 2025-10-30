package com.furniture.dao;

import com.furniture.model.CartItem; 
import com.furniture.model.Product; // Import Product model
import java.sql.*;
import java.util.List;

public class OrderDAO {

    // IMPORTANT: Check your database credentials here!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/project";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Vishal@12"; 

    private static final String INSERT_ORDER_SQL = 
        "INSERT INTO orders (customer_id, order_date, total_amount, status) VALUES (?, NOW(), ?, 'PENDING')";
  
    private static final String INSERT_ORDER_ITEM_SQL = 
        "INSERT INTO order_items (order_id, product_id, quantity, price_at_sale) VALUES (?, ?, ?, ?)";

    public boolean saveOrder(int customerId, List<CartItem> cartItems, double totalAmount) {
        Connection con = null;
        PreparedStatement pstmtOrder = null;
        PreparedStatement pstmtItem = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            con.setAutoCommit(false); // Start transaction

            // 1. Insert the main order record
            pstmtOrder = con.prepareStatement(INSERT_ORDER_SQL, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, customerId);
            pstmtOrder.setDouble(2, totalAmount);
            pstmtOrder.executeUpdate();

            rs = pstmtOrder.getGeneratedKeys();
            if (rs.next()) {
                long orderId = rs.getLong(1);

                // 2. Insert all order items (products)
                pstmtItem = con.prepareStatement(INSERT_ORDER_ITEM_SQL);
                for (CartItem item : cartItems) {
                    Product product = item.getProduct(); // Assuming CartItem has a getProduct() method

                    pstmtItem.setLong(1, orderId);
                    pstmtItem.setInt(2, product.getId());   // product_id
                    pstmtItem.setInt(3, item.getQuantity()); // quantity
                    // The price from the product is saved as the price at sale:
                    pstmtItem.setDouble(4, product.getPrice());  // price_at_sale
                    
                    pstmtItem.addBatch();
                }
                pstmtItem.executeBatch();
                
                con.commit(); // Commit transaction
                success = true;
            } else {
                con.rollback(); 
            }

        } catch (ClassNotFoundException e) {
            System.err.println("--- FATAL ORDERDAO ERROR: JDBC Driver Not Found ---");
            e.printStackTrace();
        } catch (SQLException e) {
            // ** CRITICAL: This log is what you need if it still fails **
            System.err.println("--- FATAL ORDERDAO ERROR: SQL EXCEPTION ---");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            
            if (con != null) {
                try {
                    System.err.println("Transaction rolled back.");
                    con.rollback(); 
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmtOrder != null) pstmtOrder.close();
                if (pstmtItem != null) pstmtItem.close();
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }
}
