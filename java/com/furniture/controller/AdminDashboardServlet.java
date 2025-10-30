package com.furniture.controller;

import com.furniture.dao.ProductDAO;
import com.furniture.model.Product;
import com.furniture.model.User;
// --- NEW IMPORTS for Orders ---
import com.furniture.model.Order; 
import com.furniture.model.OrderItem;
// ------------------------------

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/AdminDashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // --- DB Configuration (CONFIRMED from your code) ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/project";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Vishal@12";
    
    private static final String SELECT_ALL_USERS_SQL = "SELECT * FROM registration"; 


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- 1. Product Catalog Data (Existing Logic) ---
        ProductDAO productDAO = new ProductDAO();
        List<Product> products = productDAO.getAllProducts();
        request.setAttribute("products", products);

        // --- 2. Customer Accounts Data (Existing Logic) ---
        try {
            List<User> userList = loadAllUsersFromDb();
            request.setAttribute("users", userList);
            request.setAttribute("userCount", userList.size());
        } catch (SQLException e) {
            System.err.println("Database Error loading users in AdminDashboardServlet:");
            e.printStackTrace();
            request.setAttribute("users", new ArrayList<User>());
            request.setAttribute("userError", "Failed to load customer data due to a database error.");
        }
        
        // --- 3. Order Management Data (Main Orders List) ---
        try {
            List<Order> orderList = loadOrdersTableData(); 
            request.setAttribute("orders", orderList);
            
            if (orderList != null && !orderList.isEmpty()) {
                System.out.println("DEBUG: Orders loaded successfully! Total orders: " + orderList.size());
            } else {
                System.out.println("DEBUG: Order list is EMPTY. Check DB connection/query/table.");
            }
            
        } catch (SQLException e) {
            System.err.println("Database Error loading orders in AdminDashboardServlet:");
            e.printStackTrace();
            request.setAttribute("orders", new ArrayList<Order>());
            request.setAttribute("orderError", "Failed to load order data due to a database error: " + e.getMessage());
        }
        
        // --- 4. Order Items Raw Data (NEW LOGIC CALL) ---
        try {
            List<OrderItem> orderItemList = loadAllOrderItemsTableData(); 
            request.setAttribute("orderItemsList", orderItemList); // Attribute for JSP
            
            // --- DEBUGGING LINE FOR ORDER ITEMS ---
            if (orderItemList != null && !orderItemList.isEmpty()) {
                System.out.println("DEBUG: Order Items loaded successfully! Total items: " + orderItemList.size());
            } else {
                System.out.println("DEBUG: Order Items list is EMPTY. Check DB connection/query/table.");
            }
            // ------------------------------------
            
        } catch (SQLException e) {
            System.err.println("Database Error loading ALL order items in AdminDashboardServlet:");
            e.printStackTrace();
            request.setAttribute("orderItemsList", new ArrayList<OrderItem>());
            request.setAttribute("orderItemsError", "Failed to load raw order item data due to a database error.");
        }


        // Forward to the JSP page
        request.getRequestDispatcher("/AdminDashboard.jsp").forward(request, response);
    }
    
    
    /**
     * Helper method to fetch User data from the database. (Existing Logic)
     */
    private List<User> loadAllUsersFromDb() throws SQLException {
        List<User> userList = new ArrayList<>();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            throw new SQLException("JDBC Driver not available.", e);
        }
        
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS_SQL);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getTimestamp("created_at")); 
                
                userList.add(user);
            }
        } 
        return userList;
    }
    
    /**
     * Helper method to fetch ALL Order data. (Main Orders List)
     */
    private List<Order> loadOrdersTableData() throws SQLException {
        List<Order> orderList = new ArrayList<>();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not available.", e);
        }
        
        String sql = "SELECT o.*, COALESCE(u.name, o.fullname) AS customerName " +
                     "FROM orders o " +
                     "LEFT JOIN registration u ON o.user_id = u.id " + 
                     "ORDER BY o.order_date DESC";
        
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setCustomerName(rs.getString("customerName"));
                order.setFullname(rs.getString("fullname"));
                order.setAddress(rs.getString("address"));
                order.setPhone(rs.getString("phone"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setUserId(rs.getObject("user_id", Integer.class)); 
                
                orderList.add(order);
            }
        } 
        return orderList;
    }
    
    /**
     * Helper method to fetch ALL Order Items data. (NEW METHOD ADDED)
     */
    private List<OrderItem> loadAllOrderItemsTableData() throws SQLException {
        List<OrderItem> itemList = new ArrayList<>();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not available.", e);
        }
        
        // SQL: Select all from the order_items table
        String sql = "SELECT * FROM order_items ORDER BY order_id DESC, id ASC"; 
        
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderItem item = new OrderItem();
                // Mapping fields using confirmed column names
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id")); 
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price")); // decimal(10,2) maps to Java double
                
                itemList.add(item);
            }
        } 
        return itemList;
    }
}