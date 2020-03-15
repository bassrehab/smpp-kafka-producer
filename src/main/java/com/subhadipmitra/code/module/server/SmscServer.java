package com.subhadipmitra.code.module.server;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.subhadipmitra.code.module.controller.auto.SmscGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance of SMSC server listening on one port
 * Each instance os SMSC server has its own executors
 * This is because SMPP library shutdowns the executors when stopped
 **/
public class SmscServer {
	private static final Logger logger = LoggerFactory.getLogger(SmscServer.class);

    private DefaultSmppServer smppServer;

    public SmscServer(SmscGlobalConfiguration config, SmppServerConfiguration serverConfig) {
        this(config, serverConfig, new SmscServerThreadPoolFactory());
    }

	public SmscServer(SmscGlobalConfiguration config, SmppServerConfiguration serverConfig, SmscServerThreadPoolFactory threadPoolFactory) {
		smppServer = new DefaultSmppServer(
        		serverConfig, 
        		new SmscSmppServerHandler(config),
                threadPoolFactory.createMainExecutor(),
                threadPoolFactory.createMonitorExecutor());
	}
	
	
	public void destroy() throws Exception {
		smppServer.destroy();
		smppServer = null;
	}
	
	public void start() throws SmppChannelException {
		smppServer.start();
	}
	
	public void stop() {
		smppServer.stop();
	}


	public void printMetrics() {
		logger.info("Server {} counters: {}", smppServer.getConfiguration().getJmxDomain(), smppServer.getCounters());
	}

}
