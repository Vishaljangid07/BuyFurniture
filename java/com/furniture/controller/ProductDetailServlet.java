package com.furniture.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.Date; // Import the Date class

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.furniture.dao.ProductDAO;
import com.furniture.model.Product;



@WebServlet("/ProductDetailServlet")
public class ProductDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Use a static field to hold the last time the application started or was deployed.
    // In a real scenario, this would ideally be the product's last modified timestamp from the DB.
    // For a simple global fix, we use the server's current time on startup/deployment.
    private static final long GLOBAL_CACHE_BUSTER = new Date().getTime(); 

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String productIdParam = request.getParameter("productId");

        // Set the cache buster version for ALL resources using the static field
        request.setAttribute("cacheBuster", GLOBAL_CACHE_BUSTER);
        
        if (productIdParam != null) {
            try {
                int productId = Integer.parseInt(productIdParam);
                ProductDAO productDAO = new ProductDAO();
                Optional<Product> product = productDAO.getProductById(productId);

                if (product.isPresent()) {
                    // 1. Put the single product object into the request scope
                    request.setAttribute("product", product.get());
                    // 2. Forward to the detail page
                    request.getRequestDispatcher("/product-detail.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException e) {
                // Handle invalid ID format
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/BuyFurniture");
    }
}	