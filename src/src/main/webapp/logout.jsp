<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.*" %>
<%@ page import="webs.manage.WebUser" %>

<!DOCTYPE html>
<html>
<body>
<%
    // 쿠키 대신 세션을 invalidate() 시킨다 ...
	HttpSession sessions = request.getSession();
	if (sessions != null) {
	    WebUser user = (WebUser) sessions.getAttribute("user");
	    if (user != null)
	        sessions.invalidate();
	}

   
   // 무조건 login.html로 sendRedirect(...) 시킨다.
   response.sendRedirect("login.html");
%>
</body>
</html>