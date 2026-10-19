package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunReloadEvent extends BaseEvent implements KubeJSGunEventPoster<GunReloadEvent>, ICancellableEvent {
   private final LivingEntity entity;
   private final ItemStack gunItemStack;
   private final LogicalSide logicalSide;
   public static final Event<GunReloadEvent.Callback> CALLBACK = EventFactory.createArrayBacked(GunReloadEvent.Callback.class, callbacks -> event -> {
      for (GunReloadEvent.Callback callback : callbacks) {
         callback.post(event);
      }
   });

   public GunReloadEvent(LivingEntity entity, ItemStack gunItemStack, LogicalSide side) {
      this.entity = entity;
      this.gunItemStack = gunItemStack;
      this.logicalSide = side;
      this.postEventToKubeJS(this);
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public ItemStack getGunItemStack() {
      return this.gunItemStack;
   }

   public LogicalSide getLogicalSide() {
      return this.logicalSide;
   }

   public interface Callback {
      void post(GunReloadEvent var1);
   }
}
