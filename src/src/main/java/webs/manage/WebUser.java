package webs.manage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WebUser {
   private static String[][] validUsers = { { "test1@some.net", "test1Password" }, { "test2@some.net", "test2Password" } };
   
   private String name;
   private Calendar lastLoggedIN;
   
   public WebUser(String n) {
      this.name = n;
      this.lastLoggedIN = Calendar.getInstance();
   }
   
   public String getName() {
      return this.name + "'s name";
   }
   
   public String getLastLoggedIN() {
      return new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(lastLoggedIN.getTime());
   }
   
   public static boolean isValidUser(String id, String pw) {
      for(int i = 0; i < validUsers.length; i++) {
         if (validUsers[i][0].equals(id) && validUsers[i][1].equals(pw))
            return true;
      }
      return false;
   }
}
