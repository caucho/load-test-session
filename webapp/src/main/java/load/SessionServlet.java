package load;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

@WebServlet("/session")
public class SessionServlet extends HttpServlet
{
  private final static int size = 1024;

  private long maxSize = 0;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    HttpSession session = req.getSession(true);

    int n = req.getIntHeader("request-number");
    int m = req.getIntHeader("size-multiplier");

    if (n == -1)
      throw new IllegalStateException();

    int nullCounter = 0;
    int size = SessionServlet.size * m;

    for (int i = n; i >= 0; i--) {
      String key = Integer.toString(i);

      if (session.getAttribute(key) == null) {
        byte[] buffer = new byte[i * size];

        session.setAttribute(Integer.toString(i), buffer);
        nullCounter++;
      }
    }

    estimateSize(session);

    resp.getWriter().write(Integer.toString(nullCounter));
  }

  private void snoop(HttpSession session)
  {
    String id = session.getId();

    String appServer

      = (String) this.getServletContext().getAttribute("caucho.server-id");

    boolean isOrigin = true;

    if ("app-0".equals(appServer) && id.charAt(0) == 'b') {
      isOrigin = false;
    }
    else if ("app-1".equals(appServer) && id.charAt(0) == 'a') {
      isOrigin = false;
    }

    if (!isOrigin)
      System.out.println("doGet [" + id + "] -> [" + appServer + "]");

  }

  private void estimateSize(HttpSession session)
  {
    try {
      Enumeration<String> attributes = session.getAttributeNames();

      int size = 0;

      while (attributes.hasMoreElements()) {
        String s = attributes.nextElement();
        size += s.length();

        Object obj = session.getAttribute(s);

        if (obj instanceof byte[]) {
          size += ((byte[]) obj).length;
        }
        else {
          System.out.println("estimateSize unexpected value "
                             + obj);
        }
      }

      if (size > maxSize) {
        maxSize = size;

        System.out.println("max-size in bytes: " + maxSize);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
