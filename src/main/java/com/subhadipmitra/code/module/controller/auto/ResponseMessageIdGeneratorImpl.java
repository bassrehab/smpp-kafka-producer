package com.subhadipmitra.code.module.controller.auto;

import com.subhadipmitra.code.module.controller.core.ResponseMessageIdGenerator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles message ID generation and formating.
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 **/
@Component
public class ResponseMessageIdGeneratorImpl implements ResponseMessageIdGenerator {
	
	private AtomicLong nextMessageId;
	
	private long initialMessageIdValue;

	public ResponseMessageIdGeneratorImpl() {
		nextMessageId = new AtomicLong();		
	}

	@Override
	public long getNextMessageId() {
		return nextMessageId.incrementAndGet();				
	}

	public long getInitialMessageIdValue() {
		return initialMessageIdValue;
	}

	public void setInitialMessageIdValue(long initialMessageIdValue) {
		this.initialMessageIdValue = initialMessageIdValue;
		this.nextMessageId.set(initialMessageIdValue);
	}
	
}
