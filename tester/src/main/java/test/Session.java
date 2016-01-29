package test;

import java.util.concurrent.atomic.AtomicBoolean;

public class Session
{
  private String _sessionId;
  private long _value;
  private AtomicBoolean _inUse = new AtomicBoolean(false);
  private long _index;

  private int _requestCount = 0;

  private final int _requestCountMax;

  public Session(long index, int requestCountMax)
  {
    _index = index;
    _requestCountMax = requestCountMax;
  }

  public String getSessionId()
  {
    return _sessionId;
  }

  public void setSessionId(String sessionId)
  {
    _sessionId = sessionId;
  }

  public long getValue()
  {
    return _value;
  }

  public void setValue(long value)
  {
    _value = value;
  }

  public boolean isInUse()
  {
    return _inUse.get();
  }

  public boolean setInUse()
  {
    return _inUse.compareAndSet(false, true);
  }

  public boolean resetInUse()
  {
    return _inUse.compareAndSet(true, false);
  }

  public long getIndex()
  {
    return _index;
  }

  public void incrementRequestCount()
  {
    _requestCount++;
  }

  public int getRequestCount()
  {
    return _requestCount;
  }

  public int getRequestCountMax()
  {
    return _requestCountMax;
  }

  @Override
  public String toString()
  {
    return "Session[" + _index + ']';
  }
}
