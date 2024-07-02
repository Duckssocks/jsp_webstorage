package webs.manage;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * Servlet implementation class WebStorageServlet
 */
public abstract class WebStorageServlet extends HttpServlet {
   private static final long serialVersionUID = 1L;

   // 전체 파일을 저장하는 '/WEB-INF/StorageRoot/' 폴더에 대한 참조
   protected static File storageRoot = null;
   
   /**
    * @see Servlet#init(ServletConfig)
    */
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      
      // web.xml 파일의 <context-param>에서 'StorageRoot' 값을 읽어서 storageRoot를 초기화함
      if (storageRoot == null) {           
         String folder = this.getServletContext().getInitParameter("StorageRoot");    
         storageRoot = new File(this.getServletContext().getRealPath(folder));
         if (!storageRoot.exists())  // 만약 'StorageRoot' 폴더가 없으면 미리 생성해 놓음
            storageRoot.mkdir();
         System.out.println("[WebStorageServlet.init] StorageRoot: " + storageRoot);
      }     
   }
}
