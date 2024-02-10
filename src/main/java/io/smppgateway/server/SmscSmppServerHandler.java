package io.smppgateway.server;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import io.smppgateway.controller.auto.SmppSessionManager;
import io.smppgateway.controller.auto.SmscGlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.smppgateway.init.ServerMain.SESSION_PASSWORD;
import static io.smppgateway.init.ServerMain.SESSION_SYSTEMID;
import static io.smppgateway.init.ServerMain.isTestMode;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 *
 */

public class SmscSmppServerHandler implements SmppServerHandler  {
	
	private static final Logger logger = LoggerFactory.getLogger(SmscSmppServerHandler.class);

	private SmppSessionManager sessionManager;
	
	private SmscGlobalConfiguration config;

	public SmscSmppServerHandler(SmscGlobalConfiguration config) {
		this.config = config;
		this.sessionManager = config.getSessionManager();
	}

	@SuppressWarnings("rawtypes")
	@Override
    public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, final BaseBind bindRequest) throws SmppProcessingException {

	    if(!isTestMode){ // Disable SystemID password validation check
            if (!SESSION_SYSTEMID.equals(bindRequest.getSystemId())) {
                logger.info("Invalid SystemID Received:" + bindRequest.getSystemId());
                throw new SmppProcessingException(SmppConstants.STATUS_INVSYSID);

            }

            if (!SESSION_PASSWORD.equals(bindRequest.getPassword())) {
                logger.info("Invalid Password Received:" + bindRequest.getPassword());
                throw new SmppProcessingException(SmppConstants.STATUS_INVPASWD);
            }
        }


        sessionConfiguration.setName("Application.SMPP." + sessionConfiguration.getSystemId());
    }

    @Override
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) throws SmppProcessingException {
	    logger.info("Session created: {}", session);
        SmscSmppSessionHandler smppSessionHandler = new SmscSmppSessionHandler(session, config);
        session.serverReady(smppSessionHandler);        
    	sessionManager.addServerSession(session);
    }

    @Override
    public void sessionDestroyed(Long sessionId, SmppServerSession session) {
        logger.info("Session destroyed: {}", session);
        // print out final stats
        if (session.hasCounters()) {
            logger.info(" final session rx-submitSM: {}", session.getCounters().getRxSubmitSM());
        }
        
    	sessionManager.removeServerSession(session);
        // make sure it's really shutdown
        session.destroy();
    }

}
