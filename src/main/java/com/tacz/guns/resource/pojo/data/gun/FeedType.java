package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public enum FeedType {
   @SerializedName("magazine")
   MAGAZINE,
   @SerializedName("manual")
   MANUAL,
   @SerializedName("fuel")
   FUEL,
   @SerializedName("inventory")
   INVENTORY;
}
