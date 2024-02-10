package io.smppgateway.server;

import com.cloudhopper.smpp.util.DaemonExecutors;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 */

public class SmscServerThreadPoolFactory {

    public ThreadPoolExecutor createMainExecutor() {

        return (ThreadPoolExecutor) DaemonExecutors.newCachedDaemonThreadPool();
    }

    public ScheduledThreadPoolExecutor createMonitorExecutor() {
        return (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private AtomicInteger sequence = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });
    }
}
