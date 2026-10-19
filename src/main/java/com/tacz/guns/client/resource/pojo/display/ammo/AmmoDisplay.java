package com.tacz.guns.client.resource.pojo.display.ammo;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.client.resource.pojo.display.IDisplay;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class AmmoDisplay implements IDisplay {
   @SerializedName("model")
   private Identifier modelLocation;
   @SerializedName("texture")
   private Identifier modelTexture;
   @Nullable
   @SerializedName("slot")
   private Identifier slotTextureLocation;
   @Nullable
   @SerializedName("entity")
   private AmmoEntityDisplay ammoEntity;
   @Nullable
   @SerializedName("shell")
   private ShellDisplay shellDisplay;
   @Nullable
   @SerializedName("particle")
   private AmmoParticle particle;
   @SerializedName("tracer_color")
   private String tracerColor = "0xFFFFFF";
   @Nullable
   @SerializedName("transform")
   private AmmoTransform transform;

   public Identifier getModelLocation() {
      return this.modelLocation;
   }

   public Identifier getModelTexture() {
      return this.modelTexture;
   }

   @Nullable
   public Identifier getSlotTextureLocation() {
      return this.slotTextureLocation;
   }

   @Nullable
   public AmmoEntityDisplay getAmmoEntity() {
      return this.ammoEntity;
   }

   @Nullable
   public ShellDisplay getShellDisplay() {
      return this.shellDisplay;
   }

   @Nullable
   public AmmoParticle getParticle() {
      return this.particle;
   }

   public String getTracerColor() {
      return this.tracerColor;
   }

   @Nullable
   public AmmoTransform getTransform() {
      return this.transform;
   }

   @Override
   public void init() {
      if (this.modelTexture != null) {
         this.modelTexture = converter.idToFile(this.modelTexture);
      }

      if (this.slotTextureLocation != null) {
         this.slotTextureLocation = converter.idToFile(this.slotTextureLocation);
      }

      if (this.ammoEntity != null && this.ammoEntity.modelTexture != null) {
         this.ammoEntity.modelTexture = converter.idToFile(this.ammoEntity.modelTexture);
      }

      if (this.shellDisplay != null && this.shellDisplay.modelTexture != null) {
         this.shellDisplay.modelTexture = converter.idToFile(this.shellDisplay.modelTexture);
      }
   }
}
