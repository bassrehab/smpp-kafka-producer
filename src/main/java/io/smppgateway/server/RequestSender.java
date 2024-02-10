package io.smppgateway.server;
/**
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 */
public interface RequestSender<T extends DelayedRecord> {
	public void scheduleDelivery(T record);
	
	public void scheduleDelivery(T record, int minDelayMs, int randomDeltaMs);
}
