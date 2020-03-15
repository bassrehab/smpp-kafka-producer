package com.subhadipmitra.code.module.controller.auto;

import com.subhadipmitra.code.module.controller.core.DeliveryReceiptScheduler;
import com.subhadipmitra.code.module.controller.core.ResponseMessageIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for Setting Global Configurations
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 */
@Component
public class SmscGlobalConfiguration {

	@Autowired
	private DelayedRequestSenderImpl deliverSender;

	@Autowired
	private ResponseMessageIdGenerator messageIdGenerator;

	@Autowired
	private SmppSessionManager sessionManager;

    @Autowired
    private DeliveryReceiptScheduler deliveryReceiptScheduler;

	public DelayedRequestSenderImpl getDeliverSender() {
		return deliverSender;
	}

	public void setDeliverSender(DelayedRequestSenderImpl deliverSender) {
		this.deliverSender = deliverSender;
	}

	public ResponseMessageIdGenerator getMessageIdGenerator() {
		return messageIdGenerator;
	}

	public void setMessageIdGenerator(ResponseMessageIdGenerator messageIdGenerator) {
		this.messageIdGenerator = messageIdGenerator;
	}

	public SmppSessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SmppSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

    public DeliveryReceiptScheduler getDeliveryReceiptScheduler() {
        return deliveryReceiptScheduler;
    }

    public void setDeliveryReceiptScheduler(DeliveryReceiptScheduler deliveryReceiptScheduler) {
        this.deliveryReceiptScheduler = deliveryReceiptScheduler;
    }
}
