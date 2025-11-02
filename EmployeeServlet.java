package com.example.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class EmployeeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String empId = request.getParameter("id");

        try (Connection conn = DBUtil.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs;

            if (empId != null && !empId.isEmpty()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM employees WHERE id=?");
                ps.setInt(1, Integer.parseInt(empId));
                rs = ps.executeQuery();
            } else {
                rs = stmt.executeQuery("SELECT * FROM employees");
            }

            out.println("<h2>Employee Records</h2>");
            out.println("<form action='EmployeeServlet' method='get'>Search by ID: <input type='text' name='id'><input type='submit' value='Search'></form>");
            out.println("<table border='1'><tr><th>ID</th><th>Name</th><th>Department</th><th>Salary</th></tr>");
            while (rs.next()) {
                out.println("<tr><td>" + rs.getInt("id") + "</td><td>" + rs.getString("name") + "</td><td>"
                        + rs.getString("department") + "</td><td>" + rs.getDouble("salary") + "</td></tr>");
            }
            out.println("</table>");
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
