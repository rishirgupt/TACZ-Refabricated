package com.tacz.guns.client.resource.pojo.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Description {
   @SerializedName("texture_height")
   private int textureHeight;
   @SerializedName("texture_width")
   private int textureWidth;
   @SerializedName("visible_bounds_height")
   private float visibleBoundsHeight;
   @SerializedName("visible_bounds_width")
   private float visibleBoundsWidth;
   @SerializedName("visible_bounds_offset")
   private List<Float> visibleBoundsOffset;

   public int getTextureHeight() {
      return this.textureHeight;
   }

   public int getTextureWidth() {
      return this.textureWidth;
   }

   public float getVisibleBoundsHeight() {
      return this.visibleBoundsHeight;
   }

   public float getVisibleBoundsWidth() {
      return this.visibleBoundsWidth;
   }

   public List<Float> getVisibleBoundsOffset() {
      return this.visibleBoundsOffset;
   }
}
