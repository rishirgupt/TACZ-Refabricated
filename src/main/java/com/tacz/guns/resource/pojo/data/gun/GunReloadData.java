package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class GunReloadData {
   @SerializedName("type")
   private FeedType type = FeedType.MAGAZINE;
   @SerializedName("infinite")
   private boolean infinite = false;
   @SerializedName("feed")
   private GunReloadTime feed = new GunReloadTime();
   @SerializedName("cooldown")
   private GunReloadTime cooldown = new GunReloadTime();

   public FeedType getType() {
      return this.type;
   }

   public GunReloadTime getFeed() {
      return this.feed;
   }

   public GunReloadTime getCooldown() {
      return this.cooldown;
   }

   public boolean isInfinite() {
      return this.infinite;
   }
}
