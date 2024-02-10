package io.smppgateway.common.utilities;

import java.util.UUID;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 08/07/17.
 *
 * Generate Unique ID.
 */
public class UniqueId {

    private String uuid;

    public UniqueId(){
        this.uuid = UUID.randomUUID().toString().replaceAll("-", "");

    }

    public String getUuid(){
        return uuid;
    }


}
