package com.tacz.guns.resource.pojo;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class AmmoIndexPOJO {
   @SerializedName("name")
   private String name;
   @SerializedName("display")
   private Identifier display;
   @SerializedName("stack_size")
   private int stackSize;
   @SerializedName("tooltip")
   @Nullable
   private String tooltip;
   @SerializedName("sort")
   private int sort;

   public String getName() {
      return this.name;
   }

   public Identifier getDisplay() {
      return this.display;
   }

   public int getStackSize() {
      return this.stackSize;
   }

   @Nullable
   public String getTooltip() {
      return this.tooltip;
   }

   public int getSort() {
      return this.sort;
   }
}
