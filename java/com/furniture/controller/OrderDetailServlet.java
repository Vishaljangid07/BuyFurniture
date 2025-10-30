package com.furniture.controller;
// Make sure this package path matches your project structure

import com.furniture.model.Order;      // You need an Order model/POJO
import com.furniture.model.OrderItem;  // You need an OrderItem model/POJO

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/OrderDetailServlet")
public class OrderDetailServlet extends HttpServlet {
    // !!! Replace with your actual DB details or use your existing DBConnection utility !!!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
    private static final String DB_USER = "your_user";
    private static final String DB_PASSWORD = "your_password";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int orderId;
        try {
            orderId = Integer.parseInt(request.getParameter("orderId"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Order ID.");
            return;
        }

        Order order = fetchOrderSummary(orderId);
        List<OrderItem> items = fetchOrderItems(orderId);

        if (order != null) {
            request.setAttribute("order", order);
            request.setAttribute("orderItems", items);
            // Forward to the JSP page created in Step 2A
            request.getRequestDispatcher("/AdminOrderDetail.jsp").forward(request, response);
        } else {
            request.setAttribute("orderError", "Order not found for ID: " + orderId);
            // Go back to the dashboard with an error message
            request.getRequestDispatcher("/AdminDashboard.jsp?tab=orders").forward(request, response);
        }
    }
    
    // --- Helper to fetch data from the 'orders' table ---
    private Order fetchOrderSummary(int orderId) {
        // Reuse the JOIN logic from AdminDashboardServlet if possible
        String SQL = "SELECT o.*, COALESCE(u.name, o.fullname) AS customerName " +
                     "FROM orders o LEFT JOIN registration u ON o.user_id = u.id WHERE o.id = ?";
        Order order = null;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setInt(1, orderId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setCustomerName(rs.getString("customerName")); 
                    order.setFullname(rs.getString("fullname")); // used for shipping address
                    order.setAddress(rs.getString("address"));
                    order.setPhone(rs.getString("phone"));
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    // --- Helper to fetch data from the 'order_items' table ---
    private List<OrderItem> fetchOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String SQL = "SELECT * FROM order_items WHERE order_id = ?"; 
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setInt(1, orderId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    // Assuming your OrderItem model has the necessary setter methods
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}