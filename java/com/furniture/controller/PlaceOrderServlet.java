package com.furniture.controller;

import com.furniture.model.CartItem;
import com.furniture.dao.ProductDAO; // <--- NEW IMPORT NEEDED
import com.furniture.controller.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.List;

@WebServlet("/PlaceOrderServlet")
public class PlaceOrderServlet extends HttpServlet {

    // Instantiate the DAO
    private final ProductDAO productDAO = new ProductDAO(); // <--- NEW: ProductDAO instance

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("ViewCartServlet");
            return;
        }

        // Step 1: Get checkout form details
        String fullnameForm = request.getParameter("fullname");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        String paymentMethod = request.getParameter("paymentMethod");

        // Step 2: Get cart + total from session
        @SuppressWarnings("unchecked")
        List<CartItem> validatedCart = (List<CartItem>) session.getAttribute("validatedCartData");
        Double grandTotal = (Double) session.getAttribute("grandTotal");

        if (validatedCart == null || validatedCart.isEmpty() || grandTotal == null) {
            // Redirect back to cart with error
            response.sendRedirect("ViewCartServlet");
            return;
        }

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psItem = null;
        // PreparedStatement psStock = null; // We will use the DAO method instead of a prepared statement here

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transaction start

            // ====================================================================
            // === NEW CRITICAL STEP: DEDUCT STOCK BEFORE SAVING ORDER ===
            // ====================================================================
            for (CartItem item : validatedCart) {
                // Use the DAO method, passing the current connection to ensure 
                // it is part of the ongoing transaction.
                boolean success = productDAO.updateStock(conn, item.getProduct().getId(), item.getQuantity());
                
                if (!success) {
                    // Fail the order if stock deduction failed (race condition)
                    throw new SQLException("Stock insufficient for product ID: " + item.getProduct().getId() + ". Order cancelled.");
                }
            }
            // ====================================================================
            // === END STOCK DEDUCTION ===
            // ====================================================================

            // Step 3: Insert into orders
            String sqlOrder = "INSERT INTO orders (fullname, address, phone, payment_method, total_amount) VALUES (?, ?, ?, ?, ?)";
            psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);

            // Step 3a: Use logged-in user's name if available
            String customerName = (String) session.getAttribute("customerName");
            if (customerName == null || customerName.isEmpty()) {
                customerName = fullnameForm; // fallback to form
            }

            psOrder.setString(1, customerName);
            psOrder.setString(2, address);
            psOrder.setString(3, phone);
            psOrder.setString(4, paymentMethod);
            psOrder.setDouble(5, grandTotal);
            psOrder.executeUpdate();

            // Get generated order ID
            ResultSet rs = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            // Step 4: Insert order items
            String sqlItem = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";
            psItem = conn.prepareStatement(sqlItem);

            for (CartItem item : validatedCart) {
                psItem.setInt(1, orderId);
                psItem.setInt(2, item.getProduct().getId());
                psItem.setString(3, item.getProduct().getName());
                psItem.setInt(4, item.getQuantity());
                psItem.setDouble(5, item.getProduct().getPrice());
                psItem.addBatch();
            }

            psItem.executeBatch();
            conn.commit(); // Commit transaction (Order saved + Stock deducted)

            // Step 5: Clear cart session attributes
            session.removeAttribute("shoppingCart");
            session.removeAttribute("validatedCartData");
            session.removeAttribute("grandTotal");

            // Step 6: Set customerName again just to be sure for success page
            session.setAttribute("customerName", customerName);

            // Step 7: Redirect to success page
            response.sendRedirect("orderSuccess.jsp?orderId=" + URLEncoder.encode(String.valueOf(orderId), "UTF-8"));

        } catch (SQLException e) {
            // Catching SQLException specifically for the stock failure or database errors
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            // Use a specific error message for stock issues
            String errorMessage = e.getMessage().contains("Stock insufficient") ? "Insufficient stock for one or more items. Please check your cart." : "Order failed due to a database error.";
            session.setAttribute("checkoutError", errorMessage);
            response.sendRedirect("checkoutAddress.jsp?error=OrderFailed");
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            session.setAttribute("checkoutError", "An unexpected error occurred during order processing.");
            response.sendRedirect("checkoutAddress.jsp?error=OrderFailed");
        } finally {
            try {
                if (psOrder != null) psOrder.close();
                if (psItem != null) psItem.close();
                // if (psStock != null) psStock.close(); // Not needed if using DAO
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }
}