package test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class Tester implements Runnable
{
  /**
   * number of requests to issue per session
   */
  public final static int requestCount = 32;

  /**
   * session item size multiplier used in kb
   *
   * max byte[] in the session will be 1024 * m * request-number
   */
;  private final static int m = 320;

  private long n = 1024 * 1024 * requestCount;

  private final URL[] _urls;
  private final SessionStore _sessionStore;

  private AtomicLong _counter = new AtomicLong();
  private long _n = n;

  public Tester(URL[] urls, SessionStore sessionStore)
  {
    this._urls = urls;
    _sessionStore = sessionStore;
  }

  @Override
  public void run()
  {
    while (_counter.incrementAndGet() < _n) {

      long l = _counter.get();

      if (l % 10000 == 0)
        System.out.println(l);

      Session session = _sessionStore.next();
      if (session != null) {
        try {
          test(session);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void test(Session session) throws IOException
  {
    URL url = _urls[(int) (System.currentTimeMillis() % _urls.length)];

    HttpURLConnection http = (HttpURLConnection) url.openConnection();

    String sessionId = session.getSessionId();

    if (sessionId != null)
      http.setRequestProperty("Cookie", "JSESSIONID=" + session.getSessionId());

    http.setRequestProperty("request-number",
                            Integer.toString(session.getRequestCount()));

    http.setRequestProperty("size-multiplier", Integer.toString(m));

    http.connect();

    try {
      int responseCode = http.getResponseCode();

      byte[] buffer = new byte[1024];
      int l;

      if (responseCode == 200) {
        String cookie = http.getHeaderField("Set-Cookie");

        if (cookie != null)
          setSessionId(session, cookie);

        InputStream in = http.getInputStream();

        int sessionValueDelay = 0;

        while ((l = in.read(buffer)) > 0) {
          sessionValueDelay = Integer.parseInt(new String(buffer, 0, l));
        }

        if (sessionValueDelay > 1)
          System.out.println(session
                             + ", "
                             + session.getRequestCount()
                             + ", "
                             + sessionValueDelay);

        in.close();
      }
      else if (responseCode == 401) {
        _sessionStore.expire(session);
      }
      else {
        InputStream in = http.getErrorStream();

        while ((l = in.read(buffer)) > 0) ;

        in.close();
      }

      session.incrementRequestCount();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      if (session.getRequestCount() > session.getRequestCountMax())
        expire(session);

      session.resetInUse();

      http.disconnect();
    }
  }

  private void setSessionId(Session session, String cookie)
  {
    //Set-Cookie: JSESSIONID=aaanFzui3gP5_aD8Utbjv; path=/
    if (cookie == null)
      return;

    if (!cookie.startsWith("JSESSIONID="))
      throw new IllegalArgumentException();

    int z = cookie.indexOf(';');

    if (z == -1)
      z = cookie.length();

    String sessionId = cookie.substring(11, z);

    session.setSessionId(sessionId);
  }

  private void expire(Session session)
  {
    _sessionStore.expire(session);
  }
}
