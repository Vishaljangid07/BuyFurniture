package com.furniture.controller;

import java.io.IOException;
	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.PreparedStatement;
	import java.sql.SQLException;
	
	import javax.servlet.ServletException;
	import javax.servlet.annotation.WebServlet;
	import javax.servlet.http.HttpServlet;
	import javax.servlet.http.HttpServletRequest;
	import javax.servlet.http.HttpServletResponse;
	
	@WebServlet("/register_servlet")
	public class register_servlet extends HttpServlet {
	
	
	   
		private static final long serialVersionUID = -2543864760502756966L;
	
		@Override
	    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	            throws ServletException, IOException {
	        
	        // Set response content type
	        response.setContentType("text/html");
	        
	        // Get parameters from the form
	        String name = request.getParameter("name");
	        String email = request.getParameter("email");
	        String password = request.getParameter("password");
	        String cpassword = request.getParameter("cpassword");
	
	        // Validate input parameters
	        if (name == null || name.trim().isEmpty() ||
	            email == null || email.trim().isEmpty() ||
	            password == null || password.trim().isEmpty() ||
	            cpassword == null || cpassword.trim().isEmpty()) {
	            
	            request.setAttribute("error", "All fields are required.");
	            request.getRequestDispatcher("register.jsp").forward(request, response);
	            return;
	        }
	
	        // Check if passwords match
	        if (!password.equals(cpassword)) {
	            request.setAttribute("error", "Passwords do not match.");
	            request.getRequestDispatcher("register.jsp").forward(request, response);
	            return;
	        }
	
	        Connection con = null;
	        PreparedStatement pstmt = null;
	
	        try {
	            // Load MySQL JDBC Driver
	            Class.forName("com.mysql.cj.jdbc.Driver");
	
	            // Connect to database
	            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "Vishal@12");
	
	            // Check if email already exists
	            String checkEmailSql = "SELECT COUNT(*) FROM registration WHERE email = ?";
	            PreparedStatement checkStmt = con.prepareStatement(checkEmailSql);
	            checkStmt.setString(1, email);
	            var rs = checkStmt.executeQuery();
	            
	            if (rs.next() && rs.getInt(1) > 0) {
	                request.setAttribute("error", "Email already exists. Please use a different email.");
	                request.getRequestDispatcher("register.jsp").forward(request, response);
	                return;
	            }
	            checkStmt.close();
	
	            // Insert new user
	            String sql = "INSERT INTO registration (name, email, password) VALUES (?, ?, ?)";
	            pstmt = con.prepareStatement(sql);
	            pstmt.setString(1, name.trim());
	            pstmt.setString(2, email.trim());
	            pstmt.setString(3, password); // Consider hashing the password for security
	
	            int rowsInserted = pstmt.executeUpdate();
	
	            if (rowsInserted > 0) {
	                // Registration successful
	                System.out.println("User registered successfully: " + email);
	                request.setAttribute("success", "Registration successful! You can now login.");
	                response.sendRedirect("login.jsp");
	            } else {
	                // Insert failed
	                request.setAttribute("error", "Registration failed. Please try again.");
	                request.getRequestDispatcher("register.jsp").forward(request, response);
	            }
	
	        } catch (ClassNotFoundException e) {
	            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
	            e.printStackTrace();
	            request.setAttribute("error", "Database driver error. Please contact administrator.");
	            request.getRequestDispatcher("register.jsp").forward(request, response);
	            
	        } catch (SQLException e) {
	            System.err.println("Database error: " + e.getMessage());
	            e.printStackTrace();
	            
	            // Handle specific SQL errors
	            if (e.getMessage().contains("Duplicate entry")) {
	                request.setAttribute("error", "Email already exists. Please use a different email.");
	            } else if (e.getMessage().contains("Connection refused")) {
	                request.setAttribute("error", "Database connection failed. Please try again later.");
	            } else {
	                request.setAttribute("error", "Database error occurred. Please try again.");
	            }
	            request.getRequestDispatcher("register.jsp").forward(request, response);
	            
	        } finally {
	            // Close resources
	            try {
	                if (pstmt != null) pstmt.close();
	                if (con != null) con.close();
	            } catch (SQLException ex) {
	                System.err.println("Error closing database resources: " + ex.getMessage());
	                ex.printStackTrace();
	            }
	        }
	    }
	}