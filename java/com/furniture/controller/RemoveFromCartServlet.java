package com.furniture.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/RemoveFromCartServlet")
public class RemoveFromCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Use the same key as AddToCartServlet and ViewCartServlet
    private static final String CART_SESSION_KEY = "shoppingCart";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Get the productId to be removed
        String productIdParam = request.getParameter("productId");
        
        if (productIdParam == null || productIdParam.isEmpty()) {
            response.sendRedirect("ViewCartServlet"); 
            return;
        }

        try {
            // Convert the product ID from the form to an integer
            int productIdToRemove = Integer.parseInt(productIdParam);
            
            HttpSession session = request.getSession(false); // Get existing session
            
            if (session != null) {
                // 2. Retrieve the cart (which is a Map<ProductId, Quantity>)
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> cartMap = (Map<Integer, Integer>) session.getAttribute(CART_SESSION_KEY);

                if (cartMap != null) {
                    
                    // *** THE CORRECT DELETION LOGIC ***
                    // Remove the entry from the Map using the ProductId as the key
                    cartMap.remove(productIdToRemove); 
                    
                    // 3. Clean up the session if the cart is now empty
                    if (cartMap.isEmpty()) {
                        session.removeAttribute(CART_SESSION_KEY);
                    }
                    
                    // If you want to update the session Map explicitly (optional, since it's a reference):
                    // session.setAttribute(CART_SESSION_KEY, cartMap); 
                }
            }
            
            // 4. Redirect the user back to the cart view (which will recalculate the totals)
            response.sendRedirect(request.getContextPath() + "/ViewCartServlet?message=ItemRemoved");

        } catch (NumberFormatException e) {
            System.err.println("Invalid productId format for removal: " + productIdParam);
            response.sendRedirect("ViewCartServlet?error=InvalidProductId"); 
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Since deletion is a modification, we generally discourage GET requests.
        // It's safer to just redirect to the cart view.
        response.sendRedirect("ViewCartServlet");
    }
}