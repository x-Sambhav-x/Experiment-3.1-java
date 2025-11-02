<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>Student Attendance Portal</title></head>
<body>
  <h2>Mark Attendance</h2>
  <form action="AttendanceServlet" method="post">
    Student ID: <input type="text" name="student_id"><br><br>
    Student Name: <input type="text" name="student_name"><br><br>
    Status: 
    <select name="status">
      <option value="Present">Present</option>
      <option value="Absent">Absent</option>
    </select><br><br>
    <input type="submit" value="Submit Attendance">
  </form>
</body>
</html>
