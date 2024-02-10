package io.smppgateway.common.utilities;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 11/10/17.
 */


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonExecutors {

    /**
     * Utility method for creating a cached pool of "daemon" threads.  A daemon
     * thread does not limit the JVM from exiting if they aren't shutdown.
     *
     * @return A new cached pool of daemon threads
     */
    static public ExecutorService newCachedDaemonThreadPool() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }


}