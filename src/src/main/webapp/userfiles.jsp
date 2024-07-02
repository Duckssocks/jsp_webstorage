<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.util.*" %>
<!DOCTYPE html>
<html>
<body>
<!-- 우선 유효한 로그인 상태인지를 항상 확인하고, 유효하지 않으면 'login.html'로 sendRedirect(...) 시킨다. -->
<%
   // 먼저 'LoginOK=이메일' Cookie를 읽어서 로그인한 상태인지를 체크한다.
   boolean isValidUser = false;
   String userEmail = null;
   
   Cookie[] cookies = request.getCookies();
   if (cookies != null) {
      for (Cookie c: cookies) {
         if ("LoginOK".equals(c.getName())) {
            String value = c.getValue();
            if (value != null && value.length() > 2 && value.contains("@")) {
               isValidUser = true;
               userEmail = value;
               System.out.println("[userfiles.jsp] Cookie(LoginOK) confirmed with " + userEmail + " account.");
               
               break;
            }
         }
      }     
   }
   
   // 로그인이 유효하지 않은 상태이면, login.html 페이지로 sendRedirect(...) 한다.
   if (!isValidUser)
      response.sendRedirect("login.html");
%>
<!-- 유효한 로그인 상태임: userfiles.jsp는 파일 목록의 조회, 파일의 삭제, 파일의 업로드를 모두 지원해야 한다. -->
<%
   // ServletContext 객체를 통해 web.xml 파일에 설정되어 있는 StorageFolder 초기화 파라메터의 값을 읽어 온다.
   String rootFolder = this.getServletContext().getInitParameter("StorageRoot");
   File userFolder = new File(this.getServletContext().getRealPath(rootFolder), "[" + userEmail + "]");
   System.out.println("[userfiles.jsp] userFolder: " + userFolder);
%>
<%   
   // 1. 파일의 삭제 요청을 처리하는 경우.
   String[] toDelete = request.getParameterValues("checked");
   if (toDelete != null) {
      for (String f: toDelete)  // "checked" 입력 파라메터로 전달된 값들에 대해서 반복처리를 한다.
         new File(userFolder, f).delete();       
   }
%>
<%
   // 2. 파일의 삭제 요청을 처리하는 경우.
   Collection<Part> mParts = null;
   try {
      mParts = request.getParts();
      System.out.println("[userfiles.jsp] processing file upload.");
   } catch(Exception e) {
      // 파일 업로드 처리가 아닌 경우에 form의 enctype='multipart/form-data'가 아니면 request.getParts()가 에러를 발생시킴
      // 조용히 에러를 무시하고, 다른 처리를 진행함
      mParts = null;
   }
   
   if (mParts != null) {
      for (Part part: mParts) {
         // "upfile" 입력 파라메터로 전달된 모든 파일에 대해서 업로드 처리를 진행한다.
         if ("upfile".equals(part.getName())) {
            // 파일명에서 이름 부분(fileName)과 확장자 부분(fileExt)을 분리한다.
            String wholeName = part.getSubmittedFileName();
            String fileName = wholeName.substring(0, wholeName.lastIndexOf("."));
            String fileExt = wholeName.substring(wholeName.lastIndexOf(".") + 1);
               
            // test하는 파일의 이름이 업로드 폴더에 없을 때까지 반복한다. 
            File test = new File(userFolder, fileName + "." + fileExt);
            while(test.exists()) {
               fileName += "_New";
               test = new File(userFolder, fileName + "." + fileExt);
            }     
            part.write(test.getAbsolutePath());  
         }
      } 
   }
%>
<div style='width:400px;' align='center'> 
  <strong><ins><%= userEmail %></ins></strong>'s Uploaded Files<br><br>
</div>

<form action='userfiles.jsp' method='post'>
  <fieldset style='width:400px'>
    <legend>uploaded files</legend>
<%  
   // 3. 파일의 목록을 보여주는 경우.
   String[] files = userFolder.list();
   if (files == null || files.length == 0)
      out.println("<strong>No uploaded files.</strong><br>");
   else {
      for (String fileName: files) {           
         out.println(String.format("<input type='checkbox' id='%s' name='checked' value='%s'>", fileName, fileName));
         out.println(String.format("<label for='%s'>%s</label>", fileName, fileName));
         out.println(String.format(" [<a href='download.do?file=%s'>다운받기</a>]<br>", fileName));                     
      }
   }
%>
    <hr>
    <div style='width:400px;' align='right'> 
      <input type='submit' value='Delete'>&nbsp;&nbsp;
      <input type='reset' value='Reset'>
    </div> 	
  </fieldset><br>  
</form>
<br>
<form action='userfiles.jsp' method='post' enctype='multipart/form-data'>
  <fieldset style='width:400px'>
    <legend>Upload a new file:</legend>
    <label for='upfile'><small>Choose Files:</small></label><br>
    <input type='file' name='upfile' id='upfile' multiple/><br>
    <hr>
    <div style='width:400px;' align='right'> 
      <input type='submit' value='Upload'>&nbsp;&nbsp;
      <input type='reset' value='Clear'>
    </div> 
  </fieldset>
</form>
<br>
<div style='width:400px;' align='right'> 
  <a href='logout.jsp'>Log Out</a>&nbsp;
</div>
</body>
</html>