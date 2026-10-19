package com.tacz.guns.client.animation.statemachine;

import net.minecraft.world.item.ItemStack;

public class ThrowableAnimationStateContext extends ItemAnimationStateContext {
   private ItemStack currentItem = ItemStack.EMPTY;
   private int usingTick = 0;
   private boolean using = false;

   public void setCurrentItem(ItemStack currentItem) {
      this.currentItem = currentItem;
   }

   public int getStackCount() {
      return this.currentItem.getCount();
   }

   public int getUsingTick() {
      return this.usingTick;
   }

   public void setUsingTick(int throwTime) {
      this.usingTick = throwTime;
   }

   public boolean isUsing() {
      return this.using;
   }

   public void setUsing(boolean using) {
      this.using = using;
   }
}
