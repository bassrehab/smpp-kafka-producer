package io.smppgateway.server;

import io.smppgateway.controller.auto.SmppSessionManager;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.server.SmppServerSession;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Base class for delayed SMPP requests.
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com> on 07/09/17.
 */
public abstract class DelayedRecord implements Delayed {

    private final SmppServerSession session;
    private long deliveryTimeMs;

    public DelayedRecord() {
        session = null;
    }

    public DelayedRecord(SmppServerSession session) {
        this.session = session;
    }

    public DelayedRecord(SmppServerSession session, long deliverAfterMs) {
        this.session = session;
        this.deliveryTimeMs = System.currentTimeMillis() + deliverAfterMs;
    }

    public void setDeliverTime(int minDelayMs, int randomDeltaMs) {
        this.deliveryTimeMs = System.currentTimeMillis() + minDelayMs + (int) (Math.random() * randomDeltaMs);
    }

    public void setDeliverTime(long deliveryTimeMs) {
        this.deliveryTimeMs = deliveryTimeMs;
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.deliveryTimeMs < ((DelayedRecord) o).deliveryTimeMs) {
            return -1;
        } else if (this.deliveryTimeMs > ((DelayedRecord) o).deliveryTimeMs) {
            return 1;
        }
        return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(deliveryTimeMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public SmppServerSession getSession() {
        return session;
    }

    public abstract SmppServerSession getUsedSession(SmppSessionManager sessionManager);

    public abstract PduRequest<?> getRequest(int sequenceNumber) throws Exception;
}
