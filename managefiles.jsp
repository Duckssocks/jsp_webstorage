<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.util.*" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자 화면</title>
</head>
<body>
  
  
  <%
  String rootFolder = this.getServletContext().getInitParameter("StorageRoot");
  File userFolder = new File(this.getServletContext().getRealPath(rootFolder));
  // File userFolder = new File(this.getServletContext().getRealPath(rootFolder), "[" + userEmail + "]");
  String[] files = userFolder.list();
  %>
  
  <%
  // 1. checkbox로 선택된 유저들을 삭제하는 요청

  // 우선 checkbox로다가 삭제할 유저의 문자열들을 받아온다.
  String[] toDelete = request.getParameterValues("checked");
  if (toDelete != null) {
	  for (String s : toDelete) {
		  // 기존 디렉토리(파일) 삭제
	      new File(userFolder, s).delete();
	      // MS-DOS 파일도 삭제하도록 ..
	      String s2 = s.replaceAll("\\[|\\]", "");
	      File s2file = new File(userFolder, s2);
	      if (s2file.exists()) {
	    	  s2file.delete();
	      }
	  }
  }

  %>
<div style='width:400px;' align='center'> 
  <strong><ins>User lists</ins></strong><br><br>
</div>


<form action='managefiles.jsp' method='post'>
  <fieldset style='width:400px'>
    <legend>Users Lists</legend>
<%  
   // 유저들의 목록을 보여준다. 아래에 파일들도 보여주는 폼 작성.
   List<String> l = new ArrayList<>(); 
   for (String s : files) {
	   if (s.indexOf("[") != -1) {
		   l.add(s);
	   }
   }

   if (files == null || files.length == 0)
      out.println("<strong>There are no users.</strong><br>");
   else {
      for (String userName: l) {           
         out.println(String.format("<input type='checkbox' id='%s' name='checked' value='%s'>", userName, userName));
         out.println(String.format("<label for='%s'>%s</label>", userName, userName));
         out.println("<br>");
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

<hr>





<div style='width:400px;' align='center'> 
  <strong><ins>All Uploaded Files</ins></strong><br><br>
</div>


<form action='managefiles.jsp' method='post'>
  <fieldset style='width:400px'>
    <legend>uploaded files</legend>
<%  
   

   // 모든 유저 파일들의 폴더를 돌면서, File[] 배열에 파일을 하나씩 저장한 후, 전체 파일을 최정적으로 보여줌.
   // 배열이 아니라 해시맵으로 파일 이름과 경로를 모두 매핑해서 가지고 있다가, 나중에 전체적으로 볼 때 해당 파일 이름만 가지고도 바로 경로를 알 수 있고, 만약에 경로값을 얻어서 null이 아니라면 지우면 된다..
   // 해시맵이 이렇게나 간편한 것이었다니..
   Map<String, String> filePathMap = new HashMap<>();
   
   
   for (String user : l) {
	   File userDirectory = new File(userFolder, user);
	   File[] userFiles = userDirectory.listFiles();
	   
	   if (userFiles != null) {
		   for (File userFile : userFiles) {
			   filePathMap.put(userFile.getName(), userFile.getAbsolutePath());
		   }
	   }
   }
   
   List<String> fileNames = new ArrayList<>(filePathMap.keySet());
   
   if (fileNames.isEmpty())
	      out.println("<strong>There are no users.</strong><br>");
	   else {
	      for (String fileName : fileNames) {           
	         out.println(String.format("<input type='checkbox' id='%s' name='filechecked' value='%s'>", fileName, fileName));
	         out.println(String.format("<label for='%s'>%s</label>", fileName, fileName));
	         out.println("<br>");
	      }
	   }
   
   
	// 파일을 삭제하는 로직. (유저 x)
	String[] fileDelete = request.getParameterValues("filechecked");
	if (fileDelete != null) {
	    for (String fileName : fileDelete) {
	        String filePath = filePathMap.get(fileName);
	        if (filePath != null) {
	        	new File(filePath).delete();
	        }
	        
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



</body>
</html>