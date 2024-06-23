package io.hhplus.tdd.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {
    private ConcurrentMap<Object, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private Lock lock;

    public void lock(Object obj) {
        lock = lockMap.computeIfAbsent(obj, k -> new ReentrantLock());
        lock.lock();
    }

    public void unlock(Object obj) {
        lockMap.computeIfPresent(obj, (k, lock) -> lock.hasQueuedThreads() ? lock : null);
        lock.unlock();
    }
}