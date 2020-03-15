package com.subhadipmitra.code.module.common.utilities; /**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 20/08/17.
 */


import java.util.Calendar;

/**
 * Singleton Class that returns a periodic UUID,
 * eg. Every hour of the day.
 */

public class PeriodicUUID {
    public int hour = Calendar.getInstance().get(Calendar.HOUR);
    public String UUID = null;
    public boolean is_reset = false;

    /** Constructor */
    private PeriodicUUID() {

        Calendar now = Calendar.getInstance();
        int current_hour = now.get(Calendar.HOUR);
        if (current_hour > this.hour){
            this.hour = current_hour;
            resetUUID();
            is_reset = true;
        }
        else {
            is_reset = false;
        }


    }

    /**
     * Reset the UUID.
     */
    private void resetUUID(){
        this.UUID = java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }


    /** Static Class */
    private static class PeriodicUUIDHolder {

        private static final PeriodicUUID INSTANCE = new PeriodicUUID();

    }

    /** Get Instance */
    public static PeriodicUUID getInstance() {
        return PeriodicUUIDHolder.INSTANCE;
    }


}