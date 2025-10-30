package com.furniture.controller;

import com.furniture.dao.ProductDAO;
import com.furniture.model.Product; // Assuming this model exists
import com.google.gson.Gson; // <-- REQUIRED: Import the Gson library

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/EditProductServlet")
public class EditProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();

    // MODIFIED doGet: Returns product data as JSON for the modal/AJAX request.
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String productIdParam = request.getParameter("productId");

        // Set response headers early to ensure JSON content is returned
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (productIdParam != null) {
            try {
                int productId = Integer.parseInt(productIdParam);

                // Use the existing DAO method
                Optional<Product> optionalProduct = productDAO.getProductById(productId);

                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();

                    // Convert the Product object to JSON
                    String productJson = new Gson().toJson(product);

                    // Write the JSON response
                    response.getWriter().write(productJson);

                } else {
                    // Product not found (HTTP 404)
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Product not found.\"}");
                }

            } catch (NumberFormatException e) {
                // Invalid productId format (HTTP 400)
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid product ID format.\"}");
            }
        } else {
            // Missing productId parameter (HTTP 400)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing product ID.\"}");
        }
    }

    // doPost logic remains the same (handles the form submission/update)
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Retrieve all the updated form parameters
            int productId = Integer.parseInt(request.getParameter("id"));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            double price = Double.parseDouble(request.getParameter("price"));
            String category = request.getParameter("category");
            String imageUrl = request.getParameter("imageUrl"); // Name must match JSP form field name
            String condition = request.getParameter("condition"); // Name must match JSP form field name

            // Create a Product object with ALL updated details
            Product updatedProduct = new Product(
                productId,
                name,
                description,
                price,
                category,
                productId, imageUrl,
                condition
            );

            // Use the existing DAO update method
            boolean success = productDAO.updateProduct(updatedProduct);

            if (success) {
                // Redirect back to the dashboard with a success message
                response.sendRedirect(request.getContextPath() + "/AdminDashboard?message=ProductUpdated");
            } else {
                // Redirect back to the edit form or dashboard with an error
                response.sendRedirect(request.getContextPath() + "/AdminDashboard?error=UpdateFailed");
            }

        } catch (NumberFormatException e) {
            // Handle error if price/id are not valid numbers
             response.sendRedirect(request.getContextPath() + "/AdminDashboard?error=InvalidFormData");
        } catch (Exception e) {
             e.printStackTrace();
             response.sendRedirect(request.getContextPath() + "/AdminDashboard?error=ServerUpdateError");
        }
    }
}