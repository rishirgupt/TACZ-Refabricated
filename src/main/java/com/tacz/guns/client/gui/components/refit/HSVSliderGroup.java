package com.tacz.guns.client.gui.components.refit;

import cn.sh1rocu.tacz.util.forge.ExtendedSlider;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.util.LaserColorUtil;
import java.awt.Color;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

public class HSVSliderGroup {
   private final Inventory inventory;
   private final int gunItemIndex;
   private final AttachmentType type;
   private final HSVSliderGroup.LaserColorSlider hueSlider;
   private final HSVSliderGroup.LaserColorSlider saturationSlider;

   public HSVSliderGroup(int x, int y, int width, int height, Inventory inventory, int gunItemIndex, @NotNull AttachmentType type) {
      this.inventory = inventory;
      this.gunItemIndex = gunItemIndex;
      this.type = type;
      int color = this.getColor(type);
      float[] hsb = Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);
      this.hueSlider = new HSVSliderGroup.LaserColorSlider(x, y, width, height, this, hsb[0]);
      this.saturationSlider = new HSVSliderGroup.LaserColorSlider(x, y + 2 + height, width, height, this, hsb[1]);
   }

   public HSVSliderGroup.LaserColorSlider getHueSlider() {
      return this.hueSlider;
   }

   public HSVSliderGroup.LaserColorSlider getSaturationSlider() {
      return this.saturationSlider;
   }

   public void apply() {
      ItemStack gun = this.inventory.getItem(this.gunItemIndex);
      if (gun.getItem() instanceof IGun iGun) {
         int rgb_new = Color.HSBtoRGB((float)this.hueSlider.getValue(), (float)this.saturationSlider.getValue(), 1.0F);
         if (this.type == AttachmentType.NONE) {
            iGun.setLaserColor(gun, rgb_new);
            return;
         }

         CompoundTag tag = iGun.getAttachmentTag(gun, this.type);
         if (tag != null) {
            AttachmentItemDataAccessor.setLaserColorToTag(tag, rgb_new);
         }

         iGun.setAttachmentTag(gun, this.type, tag);
      }
   }

   private int getColor(AttachmentType type) {
      if (this.inventory == null) {
         return 16711680;
      }

      ItemStack gun = this.inventory.getItem(this.gunItemIndex);
      if (gun.getItem() instanceof IGun iGun) {
         if (type == AttachmentType.NONE) {
            return LaserColorUtil.getLaserColor(gun);
         }

         ItemStack attachment = iGun.getAttachment(Minecraft.getInstance().level.registryAccess(), gun, type);
         return LaserColorUtil.getLaserColor(attachment);
      } else {
         return 16711680;
      }
   }

   public static class LaserColorSlider extends ExtendedSlider {
      private final HSVSliderGroup parent;

      public LaserColorSlider(int x, int y, int width, int height, HSVSliderGroup parent, double current) {
         super(x, y, width, height, Component.empty(), Component.empty(), 0.0, 1.0, current, 0.01, 0, true);
         this.parent = parent;
      }

      @Override
      protected void applyValue() {
         this.parent.apply();
      }
   }
}
