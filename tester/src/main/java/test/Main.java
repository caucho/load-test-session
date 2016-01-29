package test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main
{
  private ExecutorService _executorService;
  private SessionStore _sessionStore;
  private Tester _tester;
  private int _clients;

  Main(URL[] urls, int clients)
  {
    _clients = clients;
    _executorService = Executors.newFixedThreadPool(clients);
    _sessionStore = new SessionStore();

    _tester = new Tester(urls, _sessionStore);
  }

  public void execute()
  {
    for (int i = 0; i < _clients; i++) {
      _executorService.submit(_tester);
    }

    _executorService.shutdown();
  }

  public static void main(String[] args) throws MalformedURLException
  {
    URL[] urls = new URL[args.length];

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      urls[i] = new URL(arg);
    }

    Main m = new Main(urls, 4);

    m.execute();
  }
}
