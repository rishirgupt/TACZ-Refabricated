package com.tacz.guns.resource.pojo;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class GunIndexPOJO {
   @SerializedName("name")
   private String name;
   @SerializedName("tooltip")
   @Nullable
   private String tooltip;
   @SerializedName("display")
   private Identifier display;
   @SerializedName("data")
   private Identifier data;
   @SerializedName("type")
   private String type;
   @SerializedName("item_type")
   private String itemType = "modern_kinetic";
   @SerializedName("sort")
   private int sort;

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getTooltip() {
      return this.tooltip;
   }

   public Identifier getDisplay() {
      return this.display;
   }

   public Identifier getData() {
      return this.data;
   }

   public String getType() {
      return this.type;
   }

   public String getItemType() {
      return this.itemType;
   }

   public int getSort() {
      return this.sort;
   }
}
