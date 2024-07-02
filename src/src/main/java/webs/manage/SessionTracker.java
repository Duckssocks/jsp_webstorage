package webs.manage;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class SessionTracker implements HttpSessionListener{
	private static int active = 0;
	
	public static int getActiveSessions() {
		return active;
	}
	
	public void sessionCreated(HttpSessionEvent se) {
		active++;
		HttpSession ssn = se.getSession();
		System.out.println("Session created(ID:" + ssn.getId() + ", total:" + active + ").");
	}
	
	public void sessionDestroyed(HttpSessionEvent se) {
		active--;
		HttpSession session = se.getSession();
		System.out.println("Session destroyed(ID:" + session.getId() + ", total:" + active + ").");
	}
}
