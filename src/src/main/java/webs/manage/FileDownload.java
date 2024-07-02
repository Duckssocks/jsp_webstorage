package webs.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FileDownload
 */
public class FileDownload extends WebStorageServlet {
	private static final long serialVersionUID = 1L;
   
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {            
      // 'LoginOK' cookie를 찾아서 로그인한 사용자가 누구인지 확인해야 함.
      String userEmail = null;      
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
         for (Cookie c: cookies) {
            if ("LoginOK".equals(c.getName())) {
               String value = c.getValue();
               if (value != null && value.length() > 2 && value.contains("@")) {
                  userEmail = value;
                  System.out.println("[FileDownload.doPost()] Cookie(LoginOK) confirmed with " + userEmail + " account.");
                  
                  break;
               }
            }
         }     
      }
      
      // 로그인 상태가 아닌 경우에는 login.html 페이지로 보낸다.
      if (userEmail == null)
         response.sendRedirect("login.html");
      
      // 유효한 파일 이름이 없을 경우에는 userfiles.jsp 페이지로 보낸다.
      String fileName = request.getParameter("file");
      if (fileName == null || fileName.length() == 0)
         response.sendRedirect("userfiles.jsp");
      
      // 선택된 파일이 다운로드 폴더에 잆을 경우에도 userfiles.jsp 페이지로 보낸다.
      File userFolder = new File(storageRoot, "[" + userEmail + "]");
      File downFile = new File(userFolder, fileName);
      System.out.println("[FileDownload.doPost()] DownFile = " + downFile);
      if (!downFile.exists())
         response.sendRedirect("userfiles.jsp");
      
      // 이제 해당 파일을 다운로드 해준다.
      String mimeType = getServletContext().getMimeType(downFile.getPath());
      System.out.println("[FileDownload.doPost()] mimeType = " + mimeType);
      
      response.setContentType((mimeType != null) ? mimeType : "application/octet-stream");
      response.setContentLength((int) downFile.length());
      
      // 파일 이름이 한글이면, URL 인토딩을 해줘야 파일명이 안 깨진다.
      String encFileName = URLEncoder.encode(downFile.getName(), "UTF-8");      
      response.setHeader("Content-Disposition", "attachment; filename=\"" + encFileName + "\"");

      try (FileInputStream fin = new FileInputStream(downFile);
            ServletOutputStream fout = response.getOutputStream()) {
         byte[] buffer = new byte[1024];
         int read = 0;
         while ((read = fin.read(buffer)) != -1)
            fout.write(buffer, 0, read);
      }
      
      System.out.println("[FileDownload.doPost()] file download completed.");
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      // TODO Auto-generated method stub
      doPost(req, resp);
   }

}
