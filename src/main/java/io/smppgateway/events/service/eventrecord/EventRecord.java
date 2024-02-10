package io.smppgateway.events.service.eventrecord;


import com.cloudhopper.smpp.pdu.SubmitSm;


/**
 * Class that wraps the SubmitSm PDU
 */
public class EventRecord {

	private SubmitSm evt;
	private long queueStartTime;
	private long queueEndTime;
	private long queueWaitTime;



	public SubmitSm getEvt() {
		return evt;
	}

	public void setEvt(SubmitSm evt) {
		this.evt = evt;
		this.setQueueStartTime();
	}


	public long getQueueStartTime() {
		return queueStartTime;
	}

	public void setQueueStartTime() {
		this.queueStartTime = System.nanoTime();
	}
	public void setQueueStartTime(long queueStartTime) {
		this.queueStartTime = queueStartTime;
	}

	public long getQueueEndTime() {
		return queueEndTime;
	}

	public void setQueueEndTime() {
		this.queueEndTime = System.nanoTime();
		this.queueWaitTime = (this.queueEndTime - this.queueStartTime);
	}
	public void setQueueEndTime(long queueEndTime) {
		this.queueEndTime = queueEndTime;
	}

	public long getQueueWaitTime() {
		return queueWaitTime;
	}

	public void setQueueWaitTime(long queueWaitTime) {
		this.queueWaitTime = queueWaitTime;
	}

	@Override
	public String toString() {
		return "EventRecord[" +
				"destAddress=" + evt.getDestAddress() +
				"sms=" + new String(evt.getShortMessage()) +
				']';
	}
}

