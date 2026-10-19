package com.tacz.guns.client.resource.pojo.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

public class GeometryModelLegacy {
   @SerializedName("bones")
   @Nullable
   private List<BonesItem> bones;
   @SerializedName("textureheight")
   private int textureHeight;
   @SerializedName("texturewidth")
   private int textureWidth;
   @SerializedName("visible_bounds_height")
   private float visibleBoundsHeight;
   @SerializedName("visible_bounds_width")
   private float visibleBoundsWidth;
   @SerializedName("visible_bounds_offset")
   private List<Float> visibleBoundsOffset;

   @Nullable
   public List<BonesItem> getBones() {
      return this.bones;
   }

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

   public GeometryModelLegacy deco() {
      if (this.bones != null) {
         this.bones.forEach(bonesItem -> {
            if (bonesItem.getCubes() != null) {
               bonesItem.getCubes().forEach(cubesItem -> {
                  if (!cubesItem.isHasMirror()) {
                     cubesItem.setMirror(bonesItem.isMirror());
                  }
               });
            }
         });
      }

      return this;
   }
}
