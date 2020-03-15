package com.subhadipmitra.code.module.events.service.completionservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_ENABLE_RETRIES;


/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 06/10/17.
 *
 */
public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
    /** Number of Rejected Events due to queue full */
    public long rejectedCount = 0L;

    /** Number of Retries */
    public long retries = 0L;

    /** Number of Successful Retries */
    public long successfulRetries = 0L;

    /** Number of Failed Retries */

    public long failedRetries = 0L;

    /** Logger Instance */
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());


    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.error("Rejected[obj=" +r.toString() + ", status=rejected, rejectionCount=" + rejectedCount++ +"]");

        if(SMPP_SERVICE_EVENTS_EXECUTOR_MAIN_ENABLE_RETRIES){
            try{
                retries++;
                CompletionServiceProvider.alternateExecutor.execute(r);
                logger.debug("RejectedRetry[status=started, obj="+ r.toString()+", desc=Re-started rejected entry retry]");
                successfulRetries++;
            }
            catch(Exception e) {   failedRetries++;
                logger.error("RejectedRetry[status=failed, desc=Failure to Re-execute "+e.getMessage() +"]");
            }
        }
        else{
            logger.warn("RejectedRetry[status=disabled, desc=Skipping reties for rejected queue entries..]");
        }

    }


}

