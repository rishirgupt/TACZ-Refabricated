package com.tacz.guns.client.resource.pojo.display.block;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.client.resource.pojo.display.IDisplay;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;

public class BlockDisplay implements IDisplay {
   @SerializedName("model")
   private Identifier modelLocation;
   @SerializedName("texture")
   private Identifier modelTexture;
   @SerializedName("transforms")
   private ItemTransforms transforms;

   public Identifier getModelLocation() {
      return this.modelLocation;
   }

   public Identifier getModelTexture() {
      return this.modelTexture;
   }

   public ItemTransforms getTransforms() {
      return this.transforms;
   }

   @Override
   public void init() {
      if (this.modelTexture != null) {
         this.modelTexture = converter.idToFile(this.modelTexture);
      }
   }
}
