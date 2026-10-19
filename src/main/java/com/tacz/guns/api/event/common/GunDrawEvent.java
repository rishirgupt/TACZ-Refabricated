package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunDrawEvent extends BaseEvent implements KubeJSGunEventPoster<GunDrawEvent> {
   private final LivingEntity entity;
   private final ItemStack previousGunItem;
   private final ItemStack currentGunItem;
   private final LogicalSide logicalSide;
   public static final Event<GunDrawEvent.Callback> CALLBACK = EventFactory.createArrayBacked(GunDrawEvent.Callback.class, callbacks -> event -> {
      for (GunDrawEvent.Callback callback : callbacks) {
         callback.post(event);
      }
   });

   public GunDrawEvent(LivingEntity entity, ItemStack previousGunItem, ItemStack currentGunItem, LogicalSide side) {
      this.entity = entity;
      this.previousGunItem = previousGunItem;
      this.currentGunItem = currentGunItem;
      this.logicalSide = side;
      this.postEventToKubeJS(this);
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public ItemStack getPreviousGunItem() {
      return this.previousGunItem;
   }

   public ItemStack getCurrentGunItem() {
      return this.currentGunItem;
   }

   public LogicalSide getLogicalSide() {
      return this.logicalSide;
   }

   public interface Callback {
      void post(GunDrawEvent var1);
   }
}
