package io.smppgateway.init;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.smppgateway.server.SmscServer;
import io.smppgateway.controller.auto.SmscGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static io.smppgateway.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVER_SESSION_PASSWORD;
import static io.smppgateway.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVER_MAX_CONNECTIONS;
import static io.smppgateway.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVER_WINDOW_SIZE;
import static io.smppgateway.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVER_REQUEST_TIMEOUT_MS;
import static io.smppgateway.config.initialize.ConfigurationsSourceSMPP.SMPP_SERVER_SYSTEM_ID;

/**
 * SMSC SMPP server:
 *	Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 *
 */
public class ServerMain {
	private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static final ServerMainParameters PARAMS = new ServerMainParameters();

	private List<SmscServer> smppServers = new ArrayList<SmscServer>();

	private FileSystemXmlApplicationContext context;

    public static boolean isTestMode = true;

    public static String SESSION_SYSTEMID;
    public static String SESSION_PASSWORD = SMPP_SERVER_SESSION_PASSWORD;

    public static boolean parseParameters(String[] args) {
		JCommander jCommander = new JCommander(PARAMS);
		try {
			jCommander.parse(args);
			if (PARAMS.isHelp()) {
				jCommander.usage();
				return false;
			}
		} catch (ParameterException ex) {
			logger.error(ex.toString(), ex);
			jCommander.usage();
			return false;
		}
		return true;
	}





    public ServerMain(String CONFIG_SMPP, boolean istestmode) {
        isTestMode = istestmode;
    	context = new FileSystemXmlApplicationContext(CONFIG_SMPP);
    	SmscGlobalConfiguration smscConfiguration = context.getBean(SmscGlobalConfiguration.class);

        // Set system ID from configuration
        SESSION_SYSTEMID = SMPP_SERVER_SYSTEM_ID;

    	for (Integer port : PARAMS.getSmscPortsAsIntegers()) {
            // Create server with configuration from properties
			SmscServer smscServer = new SmscServer(
                smscConfiguration,
                port,
                SMPP_SERVER_SYSTEM_ID,
                SMPP_SERVER_MAX_CONNECTIONS,
                SMPP_SERVER_WINDOW_SIZE,
                SMPP_SERVER_REQUEST_TIMEOUT_MS
            );
			smppServers.add(smscServer);
        }
	}

    public void start() throws Exception {
    	logger.info("Starting SMPP servers...");
    	for (SmscServer smppServer: smppServers) {
    		smppServer.start();
    	}
        logger.info("SMPP servers started");
    }

    public void stop() throws Exception {
    	logger.info("Stopping SMPP servers...");
    	for (SmscServer smppServer: smppServers) {
    		smppServer.stop();
    	}
        logger.info("SMPP servers stopped");
    }

    public void destroy() throws Exception {
		logger.info("Destroying SMPP servers...");
    	for (SmscServer smppServer: smppServers) {
    		smppServer.destroy();
    	}
    	logger.info("Destroying Spring context ...");
    	context.close();
    	logger.info("Done destroying");
	}

    public void printMetrics() {
    	logger.info("SMPP server metrics");
        for (SmscServer smppServer: smppServers) {
        	smppServer.printMetrics();
        }
    }

}
