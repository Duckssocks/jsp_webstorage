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

/**
 * Servlet implementation class Unsubscribe
 */
public class Unsubscribe extends WebStorageServlet {
   private static final long serialVersionUID = 1L;

   private static final int ErrorCode_NoError = 0B0;
   private static final int ErrorCode_NoSuchUser = 0B1;
   private static final int ErrorCode_BadPassword = 0B10;   
   private static final int ErrorCode_UserFile = 0B100;

   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
      PrintWriter pw = response.getWriter();
      // DB 커넥션 받기
   	  String driverName = this.getServletContext().getInitParameter("dbDriverClass");
   	  String driverURL = this.getServletContext().getInitParameter("dbURL");
      
      // 발생한 에러를 기록하기 위한 변수
      int errorCode = ErrorCode_NoError;
      
      // 먼저 전달된 입력 파라메터 값을 읽어 놓음
      String email = request.getParameter("userid");
      String passWD = request.getParameter("pw");
      
      // 사용자의 full name을 저장해 두어야 나중에 파일이 삭제된 후에도 결과 화면에 출력할 수 있음.
      String fullName = "";
      
      // 사용자 계정 파일을 열어서 비밀번호가 일치하는지 체크한다.
      File userFile = new File(storageRoot, email);
      if (!userFile.exists()) // 에러! 그런 사용자는 없음
         errorCode += ErrorCode_NoSuchUser; 
      else {
         try {
            Scanner readIn = new Scanner(userFile);
            String realPassword = readIn.nextLine();  // 첫번째 줄은 비밀번호
            String line = readIn.nextLine();         // 두번째 줄은 '이름;생년월일'
            fullName = line.substring(0, line.indexOf(';'));  // 이름을 잘라내서 읽어 냄
            readIn.close();
            
            if (!passWD.equals(realPassword))
               errorCode += ErrorCode_BadPassword;
            else { 
               // 1. 사용자의 업로드 폴더 즉, '[이메일]' 폴더 내부의 모든 파일을 삭제하고, 업로드 폴더를 지운다.
               //    사용자 파일의 삭제시 발생하는 에러는 무시하기로 함.
               String upFolderName = '[' + email + ']';
               File upFolder = new File(storageRoot, upFolderName);
               File[] userFiles = upFolder.listFiles();
               for (File f: userFiles)
                  f.delete();
               upFolder.delete();
               
               // 2. 사용자 계정 파일을 삭제함
               if (!userFile.delete())
                  errorCode += ErrorCode_UserFile;
               
               // 3. 마지막으로 혹시 설정되어 있을 수도 있는 'LoginOK' Cookie 값을 제거하도록 요청함.
               Cookie[] cookies = request.getCookies();
               if (cookies != null) {
                  for (Cookie c: cookies) {
                     if ("LoginOK".equals(c.getName()) && email.equals(c.getValue())) {
                        c.setMaxAge(0);
                        response.addCookie(c);
                        System.out.println("[Unsubacribe.doPost()] Cookie: LoginOK=" + email + " dropped.");
                        break;
                     }
                  }
               }
            }
         } catch (IOException e) {
            // 에러! 사용자 삭제 실패
            e.printStackTrace();
            errorCode += ErrorCode_UserFile;
         }
      }
      
     String realemail = "";
     String realpassword = "";
      
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
     String str = "SELECT email, password FROM StorageUser WHERE email = '" + email + "'";
     ResultSet rs = state.executeQuery(str);			            
     while (rs.next()) {
        realemail = String.valueOf(rs.getString("email"));
        realpassword = String.valueOf(rs.getString("password"));
     }
     System.out.println("DB를 통해 가져온 유저의 실제 email과 password는 " + realemail + ", " + realpassword);
     
     // 자 이제.. 가져온 email과 password랑, 입력된 email과 password를 비교해야겠지? 
     if (realemail == null) {
    	 // 에러! 그런 사용자는 없음.
    	 System.out.println("그런 사용자는 없습니다.");
    	 errorCode += ErrorCode_NoSuchUser;
    	 response.sendRedirect("Unsubscribe.html");
     } else {
    	 // 입력 password와 실제 password의 비교
    	 if (!passWD.equals(realpassword)) {
    		 errorCode += ErrorCode_BadPassword;
    	     System.out.println("기존 DB의 password와, 입력된 password간 오류가 발생하였음.");
    	 }
    	 else {
    		 // 만약 입력 password와 DB에 저장된 password가 일치한다면, DB에서 해당 파일을 삭제.
    		 String str2 = "DELETE FROM StorageUser WHERE email = '" + email + "'";
    		 state.executeQuery(str2);
    		 System.out.println("삭제된 user의 이름은 : " + fullName + "입니다.");
    		 
    		 // 쿠키 삭제는 위에서 해주었으니, 따로 하지 않아도 됨.
    	 }
     }
    } catch (SQLException e) {
 	   System.out.println("invalid SQL : check SQL");
 	   e.printStackTrace();
    }
     
     
      
      if (errorCode > 0) {  // 뭔가 에러가 발생함 ==> sample_delete_fail.html 출력
         pw.println("<html>");
         pw.println("<body>");
         pw.println("<h2>Oops!</h2>\n");
         pw.println("Account deletion failed.<br><br>");
         pw.println(String.format("ID: <strong style='color:blue;'>%s</strong><br>", email));         
         
         if ((errorCode & ErrorCode_NoSuchUser) != 0)
            pw.println("Reason: <strong style='color:red;'>등록되지 않은 사용자입니다.</strong><br><br>");
         
         if ((errorCode & ErrorCode_BadPassword) != 0)
            pw.println("Reason: <strong style='color:red;'>비밀번호가 일치하지 않거나, 적절하지 않습니다.</strong><br><br>");
         
         if ((errorCode & ErrorCode_UserFile) != 0)
            pw.println("Reason: <strong style='color:red;'>사용자 계정파일 작업이 실패했습니다.</strong><br><br>");
         
         pw.println("\nGoto: <a href='unsubscribe.html'>unsubscribe</a>\n");
         pw.println("<script type='text/javascript'>");
         pw.println("  setTimeout(function(){ window.location.href='unsubscribe.html' }, 3000);");
         pw.println("</script>");
         pw.println("</body>");
         pw.println("</html>");
      }
      else { // 에러없이 성공함 ==> sample_delete_ok.html 출력
         pw.println("<html>");
         pw.println("<body>");
         pw.println(String.format("<h2>Good bye %s!</h2>\n", fullName));
         pw.println("가입 해지가 성공적으로 처리되었습니다.<br><br>");
         pw.println(String.format("ID: <strong style='color:blue;'>%s</strong><br><br>", email));
         pw.println("\nGoto: <a href='subscribe.html'>subscribe</a>\n");                
         pw.println("<script type='text/javascript'>");
         pw.println("  setTimeout(function(){ window.location.href='subscribe.html' }, 3000);");
         pw.println("</script>");
         pw.println("</body>");
         pw.println("</html>");
      }
      
      pw.close();  
   }

}

