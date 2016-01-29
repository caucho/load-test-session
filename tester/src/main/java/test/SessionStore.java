package test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SessionStore
{
  private static long size = 1024;

  private Session[] _sessions = new Session[(int) size];
  private AtomicLong _index = new AtomicLong(0);

  private ReadWriteLock _lock = new ReentrantReadWriteLock();

  public Session next()
  {
    final long l = _index.getAndIncrement();
    final int index = (int) (l % size);

    Session result = null;
    try {
      _lock.readLock().lock();
      Session candidate = _sessions[index];

      if (candidate == null) {
        _lock.readLock().unlock();
        try {
          _lock.writeLock().lock();

          Session newSession = _sessions[index];

          if (newSession == null) {

            newSession = new Session(l, (int) (l % Tester.requestCount) + 4);

            newSession.setInUse();

            _sessions[index] = newSession;

            result = newSession;
          }

          _lock.readLock().lock();
        } finally {
          _lock.writeLock().unlock();
        }
      }
      else if (candidate.setInUse()) {
        result = candidate;
      }
    } finally {
      _lock.readLock().unlock();
    }

    return result;
  }

  public void expire(Session session)
  {
    _lock.writeLock().lock();

    int index = (int) (session.getIndex() % size);

    assert session == _sessions[index];

    _sessions[index] = null;

    _lock.writeLock().unlock();
  }
}
