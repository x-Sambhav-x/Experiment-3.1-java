package com.example.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                out.println("<h2>Welcome, " + username + "!</h2>");
                out.println("<a href='employee.html'>View Employees</a><br>");
                out.println("<a href='attendance.jsp'>Student Attendance Portal</a>");
            } else {
                out.println("<h3>Invalid username or password!</h3>");
                out.println("<a href='login.html'>Try Again</a>");
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
