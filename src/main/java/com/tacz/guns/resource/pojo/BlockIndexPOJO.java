package com.tacz.guns.resource.pojo;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class BlockIndexPOJO {
   @SerializedName("name")
   private String name;
   @SerializedName("display")
   private Identifier display;
   @SerializedName("data")
   private Identifier data;
   @SerializedName("id")
   private Identifier id = Identifier.fromNamespaceAndPath("tacz", "gun_smith_table");
   @SerializedName("stack_size")
   private int stackSize;
   @SerializedName("tooltip")
   @Nullable
   private String tooltip;

   public String getName() {
      return this.name;
   }

   public Identifier getId() {
      return this.id;
   }

   public Identifier getDisplay() {
      return this.display;
   }

   public int getStackSize() {
      return this.stackSize;
   }

   public Identifier getData() {
      return this.data;
   }

   @Nullable
   public String getTooltip() {
      return this.tooltip;
   }
}
