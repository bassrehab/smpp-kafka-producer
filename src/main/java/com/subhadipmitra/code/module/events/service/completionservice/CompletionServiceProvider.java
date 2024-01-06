package com.subhadipmitra.code.module.events.service.completionservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP.*;


public class CompletionServiceProvider {
    /** Logger Instance */
    private static Logger logger = LoggerFactory.getLogger(CompletionServiceProvider.class);

    /** Thread Factory */
	private static ThreadFactory threadFactory = Executors.defaultThreadFactory();

	/** Queue for Events */
	private static final BlockingQueue<Runnable> smppQueue = new ArrayBlockingQueue<>(SMPP_SERVICE_EVENTS_QUEUE_SIZE);

    /** Create Executor for Alternate Execution in case of first Executor shut down */
    public static ThreadPoolExecutor alternateExecutor=(ThreadPoolExecutor) Executors.newFixedThreadPool(SMPP_SERVICE_EVENTS_EXECUTOR_ALT_POOL_SIZE);

    /** Rejected Queue Execution Handler */
    private static final RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();

	/** Create the Main ThreadPoolExecutor */
	private static final ThreadPoolExecutor exec = new ThreadPoolExecutor(  SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_CORE_POOL_SIZE,
                                                                            SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_MAXIMUM_POOL_SIZE,
                                                                            SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_KEEP_ALIVE_TIME_SECS,
                                                                            TimeUnit.SECONDS, smppQueue, threadFactory, rejectionHandler);

	/** Monitor Threads Instance */
	private static MonitorThreads monitor;

    /** Create Completion Service */
	private static final CompletionService completionService = new ExecutorCompletionService<>(exec);

	/** Get Executor Obj */
	public static Executor getExec() {
		return exec;
	}

	/** Get Completion Service Obj */
	public static CompletionService getCompletionservice(String execName) {
		startMonitoring(execName);
		return completionService;
	}


	/** Start Monitoring */
	private static void startMonitoring(String execName){
		//start the monitoring thread
		monitor = new MonitorThreads(exec, SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_MONITOR_DELAY_SECS, execName, rejectionHandler);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();
	}


	/** Shutdown */
	public static void shutdown(){
	    exec.shutdown();
	    alternateExecutor.shutdown();

        try {
            if (!exec.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.error("Exec Threads didn't finish in 60 seconds!");
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for executor shutdown", e);
            Thread.currentThread().interrupt();
        }

        try {
            if (!alternateExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.error("Alternate Exec Threads didn't finish in 60 seconds!");
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for alternate executor shutdown", e);
            Thread.currentThread().interrupt();
        }

		logger.info("Reclaimed Executor resources.");

		monitor.shutdown();

		logger.info("Shutdown Executor monitoring.");
	}



}
