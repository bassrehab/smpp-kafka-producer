package io.smppgateway.events.service.completionservice;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 06/10/17.
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

import static io.smppgateway.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVICE_JVM_START_TIME;


public class MonitorThreads implements Runnable {
    /** Logger Instance */
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** Executor Def */
    private ThreadPoolExecutor executor;

    /** Delay run */
    private int seconds;

    /** Run Latch */
    private boolean run = true;

    /** Executor Name */
    private String executorName;

    /** RejectedExecutionHandler Implementation Obj */
    private RejectedExecutionHandlerImpl rejectionHandler;


    /** Constructor */
    public MonitorThreads(ThreadPoolExecutor executor, int delay, String executorName, RejectedExecutionHandlerImpl rejectionHandler) {
        this.executor = executor;
        this.seconds=delay;
        this.executorName = executorName;
        this.rejectionHandler = rejectionHandler;

    }

    /** Shutdown */
    public void shutdown(){
        this.run=false;
    }

    /** Run */
    @Override
    public void run() {
        while(run){
           logger.info(
                    String.format("Monitor-%s: SINCE={%s}, EXECUTOR={[%d/%d] Active=%d, ExecCompleted=%d, ExecTask=%d, ExecIsShutdown=%s, " +
                                    "ExecIsTerminated=%s, rejectedCount=%d, retries=%d, successfulRetries=%d, failedRetries=%d}",
                            this.executorName,
                            SMPP_SERVICE_JVM_START_TIME,
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated(),
                            this.rejectionHandler.rejectedCount,
                            this.rejectionHandler.retries,
                            this.rejectionHandler.successfulRetries,
                            this.rejectionHandler.failedRetries
                            ));
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                logger.warn("Monitor thread interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
        }

    }
}