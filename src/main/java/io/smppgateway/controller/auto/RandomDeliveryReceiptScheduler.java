package io.smppgateway.controller.auto;

import io.smppgateway.config.initialize.ConfigurationsSourceSMPP;
import io.smppgateway.controller.core.DeliveryReceiptScheduler;
import org.springframework.stereotype.Component;
import static io.smppgateway.init.ServerMain.isTestMode;
import java.util.Random;


/**
 * Class that generates random delays for generating Delivery Receipts.
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 *
 */
@Component
public class RandomDeliveryReceiptScheduler implements DeliveryReceiptScheduler {
    private int minDelayMs = isTestMode? 5000 : ConfigurationsSourceSMPP.SMPP_SERVER_DELTA_RANDOM_DELIVERY_RECEIPT_SCHEDULER_MS;
    private int randomDeltaMs =   isTestMode? 5000 : ConfigurationsSourceSMPP.SMPP_SERVER_MIN_DELAYED_RANDOM_DELIVERY_RECEIPT_SCHEDULER_MS;;
    private Random deliveryRandom = new Random();

    @Override
    public long getDeliveryTimeMillis() {
        return System.currentTimeMillis() + minDelayMs + (int) (deliveryRandom.nextDouble() * randomDeltaMs);
    }

    public int getMinDelayMs() {
        return minDelayMs;
    }

    public void setMinDelayMs(int minDelayMs) {
        this.minDelayMs = minDelayMs;
    }

    public int getRandomDeltaMs() {
        return randomDeltaMs;
    }

    public void setRandomDeltaMs(int randomDeltaMs) {
        this.randomDeltaMs = randomDeltaMs;
    }
}
