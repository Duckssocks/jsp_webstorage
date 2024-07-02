package webs.manage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import webs.manage.WebUser;

/**
 * Servlet implementation class HandleLogin
 */
public class HandleLogin extends WebStorageServlet {
   private static final long serialVersionUID = 1L;

   private static final int ErrorCode_NoError = 0B0;
   private static final int ErrorCode_BadParameters = 0B1;
   private static final int ErrorCode_BadUser = 0B10;
   private static final int ErrorCode_BadPassword = 0B100;
   private static final int ErrorCode_FileReadFailure = 0B1000;

   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
    *      response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
      PrintWriter pw = response.getWriter();
      
      
      // DB 커넥션 받기
   	  String driverName = this.getServletContext().getInitParameter("dbDriverClass");
   	  String driverURL = this.getServletContext().getInitParameter("dbURL");
   	  
      
      // 로그인을 위한 두 개의 입력 파라메터 값을 읽어 놓기로 하자.
      String userName = request.getParameter("userid");
      String passWord = request.getParameter("pw");
      System.out.println("[To check input parameters] uname = " + userName);
      System.out.println("[To check input parameters] psw = " + passWord);
      
      // 실제 이메일과 패스워드
      String realemail = "";
      String realpassword = "";

      // 발생한 에러를 기록하기 위한 변수
      int errorCode = ErrorCode_NoError;
      // 우선 자체적인 validation.
      if (userName == null || userName.indexOf("@") < 0 || passWord == null || passWord.length() == 0)
         errorCode += ErrorCode_BadParameters;
      else {
    	     // 입력된 userName이 정상이라면 ... 로그인을 할 수 있도록 처리한다.
    	     // driverName으로부터 class 로딩.
    		 try {
    				Class.forName(driverName);
    			} catch (ClassNotFoundException e) {
    				System.out.println("class loading error!");
    				e.printStackTrace();
    			}
    	  
    	     // DB 연결 받고, StorageUser 테이블에 정보 저장
    	     try (
    	  		Connection conn = DriverManager.getConnection(driverURL);
    	  		Statement state = conn.createStatement();
    	  	 ) {
    	     	
    	  	 // email, password을 DB로부터 받아온다. 
    	     String str = "SELECT email, password FROM StorageUser WHERE email = '" + userName + "'";
    	     ResultSet rs = state.executeQuery(str);			            
    	     while (rs.next()) {
    	        realemail = String.valueOf(rs.getString("email"));
    	        realpassword = String.valueOf(rs.getString("password"));
    	     }
    	     System.out.println("DB를 통해 가져온 유저의 실제 email과 password는 " + realemail + ", " + realpassword);
    	     
    	     // 가져온 email과 password를 통해 로그인 처리.
    	     if (realemail == null) {
    	    	 // 에러! 그런 사용자는 없음.
    	    	 System.out.println("아이디 혹은 password를 확인하십시오.");
    	    	 errorCode += ErrorCode_BadUser;
    	    	 response.sendRedirect("login.html");
    	     } else {
    	    	 // 입력 password와 실제 password의 비교
    	    	 if (!passWord.equals(realpassword)) {
    	    		 errorCode += ErrorCode_BadPassword;
    	    	     System.out.println("기존 DB의 password와, 입력된 password간 오류가 발생하였음.");
    	    	     System.out.println("비밀번호를 잘못 입력하셨습니다.");
    	    	 }
    	     }
    	    } catch (SQLException e) {
    	 	   System.out.println("invalid SQL : check SQL");
    	 	   errorCode += ErrorCode_BadUser;
    	 	   e.printStackTrace();
    	    }

         // else문이 끝나는 부분.
         
      }

      System.out.println("[Check Error Code] current error code = " + errorCode);
      if (errorCode > 0) { // 뭔가 에러가 발생한 경우 ==> sample_login_fail.html 출력
         pw.println("<html>");
         pw.println("<body>");
         pw.println("<h2>로그인 에러</h2>\n");
         pw.println("ID: <strong style='color:blue;'>" + userName + "</strong><br>");

         if ((errorCode & ErrorCode_BadParameters) != 0)
            pw.println("Reason: <strong style='color:red;'>입력 파라메터의 값이 유효하지 않습니다!</strong><br><br>");

         if ((errorCode & ErrorCode_BadUser) != 0 || (errorCode & ErrorCode_BadPassword) != 0)
            pw.println("Reason: <strong style='color:red;'>계정이 없거나 비밀번호가 일치하지 않습니다!</strong><br><br>");

         if ((errorCode & ErrorCode_FileReadFailure) != 0)
            pw.println("Reason: <strong style='color:red;'>시스템 오류: 계정 파일을 읽을 수 없습니다!</strong><br><br>");
         
         pw.println("\nGoto: <a href='login.html'>Login 재시도</a>\n");
         pw.println("<script type='text/javascript'>");
         pw.println("  setTimeout(function(){ window.location.href='login.html' }, 3000);");
         pw.println("</script>");
         pw.println("</body>");
         pw.println("</html>");         
      } else { 
    	  
    	  // 1. 정상적인 사용자 임이 확인됨. 세션에 사용자 정보를 저장한다.
          HttpSession session = request.getSession();
          WebUser user = (WebUser) session.getAttribute("user");
          
          if (user == null) {
          if (WebUser.isValidUser(realemail, realpassword)) {
        	  user = new WebUser(userName);
        	  session.setAttribute("user", user);
        	  pw.print("login success!<br>");
	  		  pw.println("<script type='text/javascript'>");
	  			
	  		  String url = response.encodeURL("userfiles.jsp");
	  		  String toSend = String.format("setTimeout(function() { window.opener.location='%s'; self.close(); }, 1000", url);
	  		  pw.println(toSend);			
	  		  pw.println("</script>");
	        	  
          } else {
        	  System.out.println("Invalid login trial.");
  			  pw.println("login failed!<br>");
  			  pw.println("<script type='text/javascript'>");
  			  pw.println("setTimeout(function() { location = 'login.html'; }, 1000);");
  			  pw.println("</script>");
          }
          }
          System.out.println("[HandleLogin.doPost()] Session attribute WebUser set to " + userName);
          
         // 2. userfiles.jsp 페이지로 sendRedirect(...) 시켜준다.
         response.sendRedirect("userfiles.jsp");
      }

      pw.close();
   }
}
