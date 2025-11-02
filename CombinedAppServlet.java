package com.example.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Single servlet implementing:
 *  - User Login (POST to /doLogin)
 *  - Employee listing & search (GET /employees or POST /doEmployee)
 *  - Student Attendance form & save (GET /attendance, POST /doAttendance)
 *
 * URL patterns:
 *   /            -> index (links)
 *   /login       -> login form
 *   /doLogin     -> login action (POST)
 *   /employees   -> employee page (shows search form + result table)
 *   /doEmployee  -> employee search (POST)  (optional — GET with ?id also works)
 *   /attendance  -> attendance form
 *   /doAttendance-> attendance submit (POST)
 *
 * Database: MySQL example. Update DB_URL, DB_USER, DB_PASS for your environment.
 *
 * SQL table examples (run once in MySQL):
 *
 * CREATE DATABASE webappdb;
 * USE webappdb;
 *
 * CREATE TABLE users (
 *   username VARCHAR(50) PRIMARY KEY,
 *   password VARCHAR(100)
 * );
 * INSERT INTO users VALUES ('admin','1234');
 *
 * CREATE TABLE employees (
 *   id INT PRIMARY KEY,
 *   name VARCHAR(100),
 *   department VARCHAR(100),
 *   salary DOUBLE
 * );
 * INSERT INTO employees VALUES
 * (1,'John Doe','HR',40000),
 * (2,'Jane Smith','IT',55000),
 * (3,'Bob Brown','Finance',50000);
 *
 * CREATE TABLE attendance (
 *   student_id INT,
 *   student_name VARCHAR(100),
 *   date DATE,
 *   status VARCHAR(10)
 * );
 *
 */
@WebServlet(urlPatterns = {
    "/", "/login", "/doLogin",
    "/employees", "/doEmployee",
    "/attendance", "/doAttendance"
})
public class CombinedAppServlet extends HttpServlet {

    // --- Configure DB here ---
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/webappdb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";      // change to your DB user
    private static final String DB_PASS = "root";      // change to your DB password
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    // ---------------------------

    private Connection getConnection() throws Exception {
        Class.forName(JDBC_DRIVER);
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("text/html;charset=UTF-8");
        switch (path) {
            case "/":
                showIndex(resp);
                break;
            case "/login":
                showLoginForm(resp);
                break;
            case "/employees":
                // allow id via query param ?id=#
                String idParam = req.getParameter("id");
                showEmployeePage(resp, idParam);
                break;
            case "/attendance":
                showAttendanceForm(resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Handle POST actions
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("text/html;charset=UTF-8");

        switch (path) {
            case "/doLogin":
                handleLogin(req, resp);
                break;
            case "/doEmployee":
                handleEmployeeSearch(req, resp);
                break;
            case "/doAttendance":
                handleAttendanceSubmit(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // --- HTML generators / handlers ---

    private void showIndex(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.println("<!doctype html><html><head><title>WebApp Home</title></head><body>");
        out.println("<h1>Web Application Portal</h1>");
        out.println("<ul>");
        out.println("<li><a href='login'>Login</a></li>");
        out.println("<li><a href='employees'>Employee Records (List/Search)</a></li>");
        out.println("<li><a href='attendance'>Student Attendance Portal</a></li>");
        out.println("</ul>");
        out.println("</body></html>");
    }

    private void showLoginForm(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.println("<!doctype html><html><head><title>Login</title></head><body>");
        out.println("<h2>User Login</h2>");
        out.println("<form method='post' action='doLogin'>");
        out.println("Username: <input name='username' required><br><br>");
        out.println("Password: <input type='password' name='password' required><br><br>");
        out.println("<input type='submit' value='Login'>");
        out.println("</form>");
        out.println("<p><a href='/'>Home</a></p>");
        out.println("</body></html>");
    }

    private void showEmployeePage(HttpServletResponse resp, String idParam) throws IOException {
        PrintWriter out = resp.getWriter();
        out.println("<!doctype html><html><head><title>Employees</title></head><body>");
        out.println("<h2>Employee Records</h2>");
        out.println("<form method='get' action='employees'> Search by ID: <input name='id'/> <input type='submit' value='Search'></form>");
        out.println("<form method='post' action='doEmployee'> Search by ID (POST): <input name='id'/> <input type='submit' value='Search'></form>");
        out.println("<hr/>");
        // show results
        try (Connection conn = getConnection()) {
            PreparedStatement ps;
            if (idParam != null && !idParam.trim().isEmpty()) {
                ps = conn.prepareStatement("SELECT * FROM employees WHERE id = ?");
                ps.setInt(1, Integer.parseInt(idParam.trim()));
            } else {
                ps = conn.prepareStatement("SELECT * FROM employees ORDER BY id");
            }
            ResultSet rs = ps.executeQuery();
            out.println("<table border='1' cellpadding='6' cellspacing='0'>");
            out.println("<tr><th>ID</th><th>Name</th><th>Department</th><th>Salary</th></tr>");
            boolean any = false;
            while (rs.next()) {
                any = true;
                out.printf("<tr><td>%d</td><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                        rs.getInt("id"),
                        escapeHtml(rs.getString("name")),
                        escapeHtml(rs.getString("department")),
                        rs.getDouble("salary"));
            }
            if (!any) {
                out.println("<tr><td colspan='4'>No records found.</td></tr>");
            }
            out.println("</table>");
        } catch (NumberFormatException nfe) {
            out.println("<p style='color:red;'>Invalid ID format.</p>");
        } catch (Exception e) {
            out.println("<pre style='color:red;'>Error: " + escapeHtml(e.getMessage()) + "</pre>");
            e.printStackTrace(out);
        }
        out.println("<p><a href='/'>Home</a></p>");
        out.println("</body></html>");
    }

    private void showAttendanceForm(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.println("<!doctype html><html><head><title>Attendance</title></head><body>");
        out.println("<h2>Student Attendance Portal</h2>");
        out.println("<form method='post' action='doAttendance'>");
        out.println("Student ID: <input name='student_id' required><br><br>");
        out.println("Student Name: <input name='student_name' required><br><br>");
        out.println("Status: <select name='status'><option>Present</option><option>Absent</option></select><br><br>");
        out.println("<input type='submit' value='Submit Attendance'>");
        out.println("</form>");
        out.println("<p><a href='/'>Home</a></p>");
        out.println("</body></html>");
    }

    // --- POST handlers ---

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and password required.");
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT username FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            out.println("<!doctype html><html><head><title>Login Result</title></head><body>");
            if (rs.next()) {
                out.println("<h2>Welcome, " + escapeHtml(username) + "!</h2>");
                out.println("<ul>");
                out.println("<li><a href='employees'>View Employees</a></li>");
                out.println("<li><a href='attendance'>Student Attendance Portal</a></li>");
                out.println("</ul>");
            } else {
                out.println("<h3 style='color:red;'>Invalid username or password.</h3>");
                out.println("<a href='login'>Try again</a>");
            }
            out.println("<p><a href='/'>Home</a></p>");
            out.println("</body></html>");
        } catch (Exception e) {
            out.println("<pre style='color:red;'>Error: " + escapeHtml(e.getMessage()) + "</pre>");
            e.printStackTrace(out);
        }
    }

    private void handleEmployeeSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // similar to showEmployeePage but accepts POST form
        String idParam = req.getParameter("id");
        showEmployeePage(resp, idParam);
    }

    private void handleAttendanceSubmit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        String sid = req.getParameter("student_id");
        String sname = req.getParameter("student_name");
        String status = req.getParameter("status");

        out.println("<!doctype html><html><head><title>Attendance Result</title></head><body>");
        if (sid == null || sname == null || status == null || sid.trim().isEmpty() || sname.trim().isEmpty()) {
            out.println("<h3 style='color:red;'>All fields are required.</h3>");
            out.println("<p><a href='attendance'>Back</a></p>");
            out.println("</body></html>");
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO attendance (student_id, student_name, date, status) VALUES (?, ?, ?, ?)");
            ps.setInt(1, Integer.parseInt(sid.trim()));
            ps.setString(2, sname.trim());
            ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(4, status.trim());
            int cnt = ps.executeUpdate();
            if (cnt > 0) {
                out.println("<h3>Attendance recorded successfully for " + escapeHtml(sname) + " (ID: " + escapeHtml(sid) + ")</h3>");
            } else {
                out.println("<h3 style='color:red;'>Failed to record attendance.</h3>");
            }
        } catch (NumberFormatException nfe) {
            out.println("<h3 style='color:red;'>Invalid Student ID format.</h3>");
        } catch (SQLIntegrityConstraintViolationException dup) {
            out.println("<h3 style='color:red;'>Database constraint error: " + escapeHtml(dup.getMessage()) + "</h3>");
        } catch (Exception e) {
            out.println("<pre style='color:red;'>Error: " + escapeHtml(e.getMessage()) + "</pre>");
            e.printStackTrace(out);
        }

        out.println("<p><a href='attendance'>Back</a> | <a href='/'>Home</a></p>");
        out.println("</body></html>");
    }

    // Simple HTML-escape utility to prevent broken HTML / basic XSS
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
