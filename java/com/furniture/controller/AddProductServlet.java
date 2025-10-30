package com.furniture.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/AddProduct")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1,   // 1 MB
    maxFileSize = 1024 * 1024 * 10,        // 10 MB
    maxRequestSize = 1024 * 1024 * 20      // 20 MB
)
public class AddProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // ----------------------------------------------------------------------------------
    // *** FIX: DEFINING PERMANENT EXTERNAL DIRECTORY ***
    // ----------------------------------------------------------------------------------
    
    // 1. CHOOSE YOUR PERMANENT, EXTERNAL PATH (!!! YOU MUST CHANGE THIS PATH !!!)
    // Example for Windows: "C:\\Furniture_Project_Uploads"
    private static final String UPLOAD_BASE_DIR = "C:\\Furniture_Project_Uploads"; 

    // 2. The sub-folder name. **MATCHES YOUR webapp/Rental FOLDER.**
    private static final String UPLOAD_FOLDER_NAME = "Rental";
    
    // 3. Combine to get the full physical path
    private static final String FULL_SAVE_PATH = UPLOAD_BASE_DIR + File.separator + UPLOAD_FOLDER_NAME;
    
    // ----------------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String priceStr = request.getParameter("price");
        String category = request.getParameter("category");
        String stockStr = request.getParameter("stock");
        String condition = request.getParameter("condition");

        Part filePart = request.getPart("imageFile");
        
        String fileName = (filePart != null && filePart.getSize() > 0) 
                          ? Paths.get(filePart.getSubmittedFileName()).getFileName().toString() 
                          : null;

        String relativePath = null;

        // ✅ Handle Image Upload
        if (fileName != null && !fileName.isEmpty()) {
            
            // 1. Ensure the permanent external directory exists
            File saveDir = new File(FULL_SAVE_PATH);
            if (!saveDir.exists()) saveDir.mkdirs(); 

            // 2. Define the final file path in the permanent location
            String filePath = FULL_SAVE_PATH + File.separator + fileName;
            
            // 3. Save the file to the permanent disk location
            try (InputStream fileContent = filePart.getInputStream()) {
                Files.copy(fileContent, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // 4. Set the path for the database: "Rental/wardrobe.jpg"
            relativePath = UPLOAD_FOLDER_NAME + "/" + fileName; 
        }

        // ✅ Validate fields (your original validation remains)
        if (name == null || name.trim().isEmpty() || priceStr == null || priceStr.trim().isEmpty()) {
            request.setAttribute("error", "Name and Price are required fields.");
            request.getRequestDispatcher("AdminDashboard.jsp").forward(request, response);
            return;
        }

        double price = 0;
        int stock = 0;
        try {
            price = Double.parseDouble(priceStr);
            stock = (stockStr != null && !stockStr.isEmpty()) ? Integer.parseInt(stockStr) : 0;
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid numeric format for price or stock.");
            request.getRequestDispatcher("AdminDashboard.jsp").forward(request, response);
            return;
        }

        // ✅ Insert product into DB
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO products (name, description, price, category, stock, image_url, product_condition) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDouble(3, price);
            ps.setString(4, category);
            ps.setInt(5, stock);
            ps.setString(6, relativePath); 
            ps.setString(7, condition);

            int rowsInserted = ps.executeUpdate();

            if (rowsInserted > 0) {
                response.sendRedirect("AdminDashboard.jsp?status=success");
            } else {
                request.setAttribute("error", "Failed to add product. Try again.");
                request.getRequestDispatcher("AdminDashboard.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Database Error: " + e.getMessage());
            request.getRequestDispatcher("AdminDashboard.jsp").forward(request, response);
        }
    }
}