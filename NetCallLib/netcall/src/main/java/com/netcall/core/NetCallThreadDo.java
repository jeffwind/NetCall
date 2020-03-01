package com.netcall.core;

import com.netcall.util.HandlerUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NetCall专用线程池
 */
public class NetCallThreadDo {

    /** 核心线程数 */
    private static final int CORE_THREAD_SIZE = 1;
    /** 最多线程数 */
    private static final int MAX_THREAD_SIZE = Integer.MAX_VALUE;
    /** 非核心线程存活时间 */
    private static final int ALIVE_TIME = 60000;
    /** 每个线程的Runnable队列数 */
    private static final int QUEUE_CAPACITY = 1;

    private static NetCallThreadDo instance;

    private ExecutorService executorService;

    private NetCallThreadDo(int coreThreadSize, int maxThreadSize, long aliveTime, int queueCapacity) {
        executorService = new ThreadPoolExecutor(coreThreadSize, maxThreadSize,
                aliveTime, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueCapacity), new MyThreadFactory());
    }

    /**
     * 初始化，如果不调用会自动初始化
     * @param coreThreadSize 核心线程数量
     * @param maxThreadSize 最大线程数量
     * @param aliveTime 非核心线程空闲时存活时间
     * @param queueCapacity 任务队列容量
     */
    public static void init(int coreThreadSize, int maxThreadSize, long aliveTime, int queueCapacity) {
        if (instance != null) {
            instance.executorService.shutdown();
            instance = null;
        }
        if (instance == null) {
            synchronized (NetCallThreadDo.class) {
                if (instance == null) {
                    instance = new NetCallThreadDo(coreThreadSize, maxThreadSize, aliveTime, queueCapacity);
                }
            }
        }
    }

    private static NetCallThreadDo getInstance() {
        if (instance == null) {
            init(CORE_THREAD_SIZE, MAX_THREAD_SIZE, ALIVE_TIME, QUEUE_CAPACITY);
        }
        return instance;
    }

    public static void execute(Runnable runnable) {
        getInstance().executorService.execute(runnable);
    }

    public static void execute(Runnable threadRunnable, Runnable mainRunnable) {
        getInstance().executorService.execute(new Runnable() {
            @Override
            public void run() {
                threadRunnable.run();
                HandlerUtil.getMain().post(mainRunnable);
            }
        });
    }

    /**
     * The default thread factory.
     */
    private static class MyThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        MyThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "NetCall-" + poolNumber.getAndIncrement() + "-Thread";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
