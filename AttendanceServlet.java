package com.example.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.time.LocalDate;

public class AttendanceServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String sid = request.getParameter("student_id");
        String name = request.getParameter("student_name");
        String status = request.getParameter("status");

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO attendance VALUES (?, ?, ?, ?)");
            ps.setInt(1, Integer.parseInt(sid));
            ps.setString(2, name);
            ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(4, status);
            ps.executeUpdate();

            out.println("<h3>Attendance recorded successfully!</h3>");
            out.println("<a href='attendance.jsp'>Back</a>");
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
