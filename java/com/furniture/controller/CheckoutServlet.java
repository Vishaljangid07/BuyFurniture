package com.furniture.controller;

import com.furniture.dao.ProductDAO;
import com.furniture.model.Product;
import com.furniture.model.CartItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/CheckoutServlet")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();

    private static final String RAW_CART_KEY = "shoppingCart";
    private static final String VALIDATED_CART_KEY = "validatedCartData";
    
    // Define the error key for consistent use in the session
    private static final String ERROR_KEY = "checkoutError"; 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        
        // 1. Security Check
        Integer customerId = (session != null)
                             ? (Integer) session.getAttribute("customerLoggedInId")
                             : null;

        if (customerId == null) {
            response.sendRedirect(contextPath + "/login.jsp?redirect=checkout");
            return;
        }

        // 2. Retrieve Raw Cart Data
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> rawCartMap = (session != null)
            ? (Map<Integer, Integer>) session.getAttribute(RAW_CART_KEY)
            : null;

        if (rawCartMap == null || rawCartMap.isEmpty()) {
            session.setAttribute(ERROR_KEY, "Your shopping cart is empty. Nothing to checkout.");
            // *** CRITICAL CHANGE: Redirect to the correct servlet which handles displaying cart.jsp ***
            response.sendRedirect(contextPath + "/ViewCartServlet");
            return;
        }

        // 3. Rebuild, Validate Cart List, AND PERFORM STOCK CHECK
        List<CartItem> validatedCartItems = new ArrayList<>();
        double grandTotal = 0.0;
        String stockError = null; 

        try {
            for (Map.Entry<Integer, Integer> entry : rawCartMap.entrySet()) {
                int productId = entry.getKey();
                int requestedQuantity = entry.getValue();

                Optional<Product> optionalProduct = productDAO.getProductById(productId);

                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();
                    int availableStock = product.getStock(); 

                    // *** CRITICAL STOCK CHECK ***
                    if (requestedQuantity > availableStock) {
                        stockError = "We only have " + availableStock + " unit(s) of " + product.getName() 
                                     + " in stock. Please adjust your quantity.";
                        break; 
                    }
                    // *** END CRITICAL STOCK CHECK ***
                    
                    CartItem item = new CartItem(product, requestedQuantity);
                    validatedCartItems.add(item);
                    // Assuming getTotalPrice() calculates item.getQuantity() * item.getProduct().getPrice()
                    grandTotal += item.getTotalPrice(); 
                } else {
                    System.err.println("Product ID " + productId + " in cart is unavailable and will be skipped.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing cart items for checkout: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute(ERROR_KEY, "An internal error occurred while validating cart data.");
            response.sendRedirect(contextPath + "/ViewCartServlet");
            return;
        }
        
        // 3A. Handle Stock Failure Immediately
        if (stockError != null) {
            session.setAttribute(ERROR_KEY, stockError);
            // *** CRITICAL CHANGE: Redirect to the correct servlet which handles displaying cart.jsp ***
            response.sendRedirect(contextPath + "/ViewCartServlet"); 
            return;
        }

        // 3B. Handle Empty Validated Cart
        if (validatedCartItems.isEmpty()) {
            session.setAttribute(ERROR_KEY, "No valid items remain in your cart for checkout.");
            response.sendRedirect(contextPath + "/ViewCartServlet");
            return;
        }
        
        // 4. Store Validated Data and Forward to Address Page
        session.setAttribute(VALIDATED_CART_KEY, validatedCartItems);
        session.setAttribute("grandTotal", grandTotal);
        
        // 5. SUCCESS: Redirect to the address selection page
        System.out.println("Checkout validation successful. Forwarding to address selection.");
        response.sendRedirect(contextPath + "/checkoutAddress.jsp");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/ViewCartServlet");
    }
}