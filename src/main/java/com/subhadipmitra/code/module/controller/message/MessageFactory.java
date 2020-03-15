package com.subhadipmitra.code.module.controller.message;

import com.cloudhopper.smpp.pdu.PduRequest;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 * @param <T>
 */
public interface MessageFactory<T extends PduRequest> {
	T createMessage() throws Exception;
}
