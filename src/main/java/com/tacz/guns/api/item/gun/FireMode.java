package com.tacz.guns.api.item.gun;

import com.google.gson.annotations.SerializedName;

public enum FireMode {
   @SerializedName("auto")
   AUTO,
   @SerializedName("semi")
   SEMI,
   @SerializedName("burst")
   BURST,
   @SerializedName("unknown")
   UNKNOWN;
}
