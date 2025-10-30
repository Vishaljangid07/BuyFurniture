package com.furniture.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/AddToCartServlet")
public class AddToCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // The key used to store the cart Map (Map<ProductId, Quantity>) in the user's session
    private static final String CART_SESSION_KEY = "shoppingCart";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get Product ID and Quantity from the form
        String productIdParam = request.getParameter("productId");
        String quantityParam = request.getParameter("quantity");
        
        int productId = 0;
        int quantity = 1;

        try {
            if (productIdParam != null) {
                productId = Integer.parseInt(productIdParam);
            }
            if (quantityParam != null && !quantityParam.trim().isEmpty()) {
                quantity = Integer.parseInt(quantityParam);
            }
            if (quantity <= 0) {
                quantity = 1; // Sanitize input
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/ProductListing?error=InvalidQuantityOrId");
            return;
        }
        
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/ProductListing?error=MissingProduct");
            return;
        }
        
        // 2. Access the user's session
        HttpSession session = request.getSession();
        
        // 3. Retrieve or Initialize the Shopping Cart Map
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute(CART_SESSION_KEY);

        if (cart == null) {
            // Cart doesn't exist yet, create a new one
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }

        // 4. Update the cart logic
        if (cart.containsKey(productId)) {
            // Product exists: increase the quantity
            int existingQuantity = cart.get(productId);
            cart.put(productId, existingQuantity + quantity);
        } else {
            // New product: add it to the cart
            cart.put(productId, quantity);
        }

        // 5. Redirect to the View Cart Servlet (Phase 2)
        response.sendRedirect(request.getContextPath() + "/ViewCartServlet?message=ItemAdded");
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect GET requests to prevent accidental cart modification
        response.sendRedirect(request.getContextPath() + "/ProductListing");
    }
}