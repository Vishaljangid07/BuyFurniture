package com.furniture.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Map to both standard LoginServlet and your existing login_servlet URL
@WebServlet({"/LoginServlet", "/login_servlet"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Database details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/project";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Vishal@12"; 

    // SQL: Retrieve user by email/password
    private static final String SELECT_USER_SQL = 
        "SELECT id, name, email FROM registration WHERE email = ? AND password = ?";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String redirectTarget = request.getParameter("redirect"); // optional redirect

        // Validate input
        if (email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            request.setAttribute("error", "Email and Password are required.");
            String forwardUrl = "/login.jsp";
            if (redirectTarget != null) forwardUrl += "?redirect=" + redirectTarget;
            request.getRequestDispatcher(forwardUrl).forward(request, response);
            return;
        }

        try (
            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        ) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (PreparedStatement pstmt = con.prepareStatement(SELECT_USER_SQL)) {
                pstmt.setString(1, email.trim());
                pstmt.setString(2, password.trim());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // --- SUCCESSFUL LOGIN ---
                        HttpSession session = request.getSession(true);
                        
                        int userId = rs.getInt("id");
                        String name = rs.getString("name");
                        String userEmail = rs.getString("email");

                        // Store session attributes for checkout & UI
                        session.setAttribute("isLoggedIn", true);
                        session.setAttribute("userName", name);          // for general UI
                        session.setAttribute("userEmail", userEmail);
                        session.setAttribute("customerLoggedInId", userId); // required by CheckoutServlet
                        session.setAttribute("customerName", name);      // required by PlaceOrderServlet / success page

                        // Redirect based on target
                        String targetUrl;
                        if ("checkout".equalsIgnoreCase(redirectTarget)) {
                            targetUrl = request.getContextPath() + "/CheckoutServlet";
                        } else {
                            targetUrl = request.getContextPath() + "/Homepage.jsp";
                        }
                        response.sendRedirect(targetUrl);

                    } else {
                        // --- FAILED LOGIN ---
                        request.setAttribute("error", "Invalid email or password.");
                        String forwardUrl = "/login.jsp";
                        if (redirectTarget != null) forwardUrl += "?redirect=" + redirectTarget;
                        request.getRequestDispatcher(forwardUrl).forward(request, response);
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            request.setAttribute("error", "System error. Please try again later.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
