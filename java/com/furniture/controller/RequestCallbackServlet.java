package com.furniture.controller;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/RequestCallbackServlet")
public class RequestCallbackServlet extends HttpServlet {
    private static final long serialVersionUID = -3558985940660352357L;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String phone = req.getParameter("phone");
        String message = req.getParameter("message");
        String statusMsg = "";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/project", "root", "Vishal@12");
            String sql = "INSERT INTO callback_requests (name, phone, message) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, message);
            int status = ps.executeUpdate();
            
            if (status > 0) {
                statusMsg = "Your request has been submitted successfully.";
            } else {
                statusMsg = "Failed to submit. Please try again.";
            }
            con.close();
            
        } catch (SQLException e) {
            // Handle specific SQL exceptions (e.g., table not found, connection failure)
            e.printStackTrace();
            statusMsg = "Database error: Could not save your request.";
            
        } catch (Exception e) {
            // Handle ClassNotFoundException (JDBC Driver) and other general exceptions
            e.printStackTrace();
            statusMsg = "Technical error! Could not process your request.";
        }
        
        // --- UPDATED LOGIC TO REDIRECT TO HOMEPAGE.JSP ---
        try {
            // 1. URL encode the message so it passes safely in the URL
            String encodedStatusMsg = java.net.URLEncoder.encode(statusMsg, "UTF-8");
            
            // 2. Redirect the user back to the main page with the status message
            // The Homepage.jsp will use JavaScript to read this parameter.
            resp.sendRedirect("Homepage.jsp?statusMsg=" + encodedStatusMsg);
            
        } catch (IOException e) {
            e.printStackTrace();
            // If redirect fails, just write an error message to the response stream
            resp.getWriter().write("Error during redirect.");
        }
        // --- END OF UPDATED LOGIC ---

        // The following lines are removed because they cause the JSP file not found error:
        // req.setAttribute("statusMsg", statusMsg);
        // req.getRequestDispatcher("requestCallback.jsp").forward(req, resp);
    }
}