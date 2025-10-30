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
import java.util.HashMap;
import java.util.List; 
import java.util.Map;
import java.util.Optional; 


@WebServlet("/ViewCartServlet")
public class ViewCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ProductDAO productDAO = new ProductDAO(); 
    private static final String CART_SESSION_KEY = "shoppingCart";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false); 
        
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cartMap = (session != null) 
            ? (Map<Integer, Integer>) session.getAttribute(CART_SESSION_KEY) 
            : null;

        List<CartItem> cartItemsList = new ArrayList<>();
        double grandTotal = 0.0;
        
        if (cartMap != null && !cartMap.isEmpty()) {
            
            for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                
                Optional<Product> optionalProduct = productDAO.getProductById(productId);

                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();
                    
                    CartItem item = new CartItem(product, quantity);
                    cartItemsList.add(item);
                    
                    grandTotal += item.getTotalPrice();
                } else {
                    System.err.println("Product ID " + productId + " found in cart but not in database. Skipping.");
                }
            }
        }
        
        if (cartMap == null) {
            cartMap = new HashMap<>(); 
        }

        request.setAttribute("cartItemsList", cartItemsList);
        request.setAttribute("grandTotal", grandTotal);

        request.getRequestDispatcher("/cart.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}