package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public enum Bolt {
   @SerializedName("open_bolt")
   OPEN_BOLT,
   @SerializedName("closed_bolt")
   CLOSED_BOLT,
   @SerializedName("manual_action")
   MANUAL_ACTION;
}
