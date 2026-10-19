package com.tacz.guns.client.resource.pojo.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

public class BonesItem {
   @SerializedName("cubes")
   private List<CubesItem> cubes;
   @SerializedName("name")
   private String name;
   @SerializedName("pivot")
   private List<Float> pivot;
   @SerializedName("rotation")
   private List<Float> rotation;
   @SerializedName("parent")
   private String parent;
   @SerializedName("mirror")
   private boolean mirror = false;

   @Nullable
   public List<CubesItem> getCubes() {
      return this.cubes;
   }

   public String getName() {
      return this.name;
   }

   public List<Float> getPivot() {
      return this.pivot;
   }

   public List<Float> getRotation() {
      return this.rotation;
   }

   public String getParent() {
      return this.parent;
   }

   public boolean isMirror() {
      return this.mirror;
   }

   @Override
   public String toString() {
      return "BonesItem{cubes = '"
         + this.cubes
         + "',name = '"
         + this.name
         + "',pivot = '"
         + this.pivot
         + "',rotation = '"
         + this.rotation
         + "',parent = '"
         + this.parent
         + "'}";
   }
}
