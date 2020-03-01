package com.netcall.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SyncLock<T> {

    private final Integer SYNC_SAM = Integer.valueOf(9999);
    private Map<T, Item> syncMap = new HashMap<>();

    public void lock(T obj) {
        synchronized (SYNC_SAM) {
            Item item;
            if (syncMap.containsKey(obj)) {
                item = syncMap.get(obj);
            } else {
                item = new Item();
                syncMap.put(obj, item);
            }
            item.count.incrementAndGet();
            item.lock.lock();
        }
    }

    public void unlock(T obj) {
        synchronized (SYNC_SAM) {
            if (!syncMap.containsKey(obj)) {
                return;
            }
            Item item = syncMap.get(obj);
            if (item.count.decrementAndGet() == 0) {
                syncMap.remove(obj);
            }
            item.lock.unlock();
        }
    }

    private class Item {
        private ReentrantLock lock = new ReentrantLock();
        private FaultAtomicInteger count = new FaultAtomicInteger();

        private class FaultAtomicInteger {

            private int integer;

            public int incrementAndGet() {
                return ++integer;
            }

            public int decrementAndGet() {
                return --integer;
            }
        }
    }
}
