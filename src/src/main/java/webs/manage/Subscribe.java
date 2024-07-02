package webs.manage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Subscribe
 */
public class Subscribe extends WebStorageServlet {

   private static final long serialVersionUID = 1L;

   private static final int ErrorCode_NoError = 0B0;
   private static final int ErrorCode_Email = 0B1;
   private static final int ErrorCode_Password = 0B10;   
   private static final int ErrorCode_UserDuplication = 0B100;
   private static final int ErrorCode_UserCreation = 0B1000; 

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
      String fullName = request.getParameter("fullname");
      String dob = request.getParameter("birthdate");
      String passWD = request.getParameter("pw");
      String passWD2 = request.getParameter("pw2");

      // check email error
      if (email == null || email.length() == 0 || email.indexOf("@") == -1)
         errorCode += ErrorCode_Email;
      
      // check password error
      if (passWD == null || passWD.length() == 0 || !passWD.equals(passWD2))
         errorCode += ErrorCode_Password;
      
      // fullName과 birthdate는 선택입력사항이므로, null일 경우 빈 문자열("")로 처리한다.
      fullName = (fullName == null) ? "" : fullName;
      dob = (dob == null) ? "" : dob;
      
      // 아직까지 에러가 없다면, 즉 Input Form으로 받은 값에 문제가 없다면, 사용자용 계정파일과 업로드 폴더를 생성함
      if (errorCode == 0) {
         File userFile = new File(storageRoot, email);
         if (userFile.exists()) // 에러! 이미 가입한 사용자임
            errorCode += ErrorCode_UserDuplication; 
         else {
            try {
               FileWriter writer = new FileWriter(userFile);
               writer.append(passWD + "\n");            // 첫 줄에 password
               writer.append(fullName + ';' + dob);  // 두번째 줄에 "이름;생년월일" 출력
               writer.close();
               
               // 사용자의 업로드 폴더를 이름이 '[email]' 형태가 되도록 생성함. 
               String upFolderName = "[" + email + "]";
               File upFolder = new File(storageRoot, upFolderName);
               if (!upFolder.mkdir())
                  throw new IOException("User Upload Folder Creation Failed.");               
            } catch (IOException e) {
               // 에러! 사용자 계정 파일 생성 실패
               e.printStackTrace();
               errorCode += ErrorCode_UserCreation;
            }
         }
      }
       // 혹시 모르니 id도 전역변수로 설정하자.
	   String id = "";
	   
       // 사용자용 계정파일과 업로드 폴더를 생성하는 과정은 그대로 가되, DB에도 해당 내용 저장 ~ (subscribe 하는 과정)
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
       	
    	// email, password, birth_date, full_name 을 INSERT하기. DB에 !
       String str = "";
       str = String.format("INSERT INTO StorageUser (email, password, birth_date, full_name) VALUES ('%s', '%s', '%s', '%s')", email, passWD, dob, fullName);
       System.out.println("인서트문은 다음과 같다. " + str);
       state.executeUpdate(str);
           
       String str2 = "SELECT id FROM StorageUser WHERE email = '" + email + "'";
       ResultSet rs2 = state.executeQuery(str2);			            
       while (rs2.next()) {
          id = String.valueOf(rs2.getInt("id"));
       }
       System.out.println(email + "유저의 id 값은 " + id + "입니다");

      } catch (SQLException e) {
   	   System.out.println("invalid SQL : check SQL");
   	   e.printStackTrace();
      }
      
      
      if (errorCode > 0) {  // 뭔가 에러가 발생함
         pw.println("<html>");
         pw.println("<body>");
         pw.println("<h2>Oops!</h2>\n");
         pw.println("Account creation failed.<br><br>\r\n");
         pw.println(String.format("ID: <strong style='color:blue;'>%s</strong><br>", email));         
         
         if ((errorCode & ErrorCode_Email) != 0)
            pw.println("Reason: <strong style='color:red;'>계정이 이메일 형식이 아닙니다.</strong><br><br>");
         
         if ((errorCode & ErrorCode_Password) != 0)
            pw.println("Reason: <strong style='color:red;'>비밀번호가 일치하지 않거나, 적절하지 않습니다.</strong><br><br>");
         
         if ((errorCode & ErrorCode_UserDuplication) != 0)
            pw.println("Reason: <strong style='color:red;'>이미 가입한 사용자입니다.</strong><br><br>");
         
         if ((errorCode & ErrorCode_UserCreation) != 0)
            pw.println("Reason: <strong style='color:red;'>사용자 계정의 생성에 실패했습니다.</strong><br><br>");
         
         pw.println("\nGoto: <a href='subscribe.html'>subscribe</a>\n");
         pw.println("<script type='text/javascript'>");
         pw.println("  setTimeout(function(){ window.location.href='subscribe.html' }, 3000);");
         pw.println("</script>");
         pw.println("</body>");
         pw.println("</html>");
      }
      else { // 에러없이 성공함
         pw.println("<html>");
         pw.println("<body>");
         pw.println(String.format("<h2>Welcome %s!</h2>\n", fullName));
         pw.println("Your account successfully created.<br><br>");
         pw.println(String.format("ID: <strong style='color:blue;'>%s</strong><br><br>", email));
         pw.println("\nGoto: <a href='login.html'>login</a>\n");                
         pw.println("<script type='text/javascript'>");
         pw.println("  setTimeout(function(){ window.location.href='login.html' }, 3000);");
         pw.println("</script>");
         pw.println("</body>");
         pw.println("</html>");
      }
      
      pw.close();  
   }

}
