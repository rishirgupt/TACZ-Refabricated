package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunFireSelectEvent extends BaseEvent implements KubeJSGunEventPoster<GunFireSelectEvent>, ICancellableEvent {
   private final LivingEntity shooter;
   private final ItemStack gunItemStack;
   private final LogicalSide logicalSide;
   public static final Event<GunFireSelectEvent.Callback> CALLBACK = EventFactory.createArrayBacked(GunFireSelectEvent.Callback.class, callbacks -> event -> {
      for (GunFireSelectEvent.Callback callback : callbacks) {
         callback.post(event);
      }
   });

   public GunFireSelectEvent(LivingEntity shooter, ItemStack gunItemStack, LogicalSide side) {
      this.shooter = shooter;
      this.gunItemStack = gunItemStack;
      this.logicalSide = side;
      this.postEventToKubeJS(this);
   }

   public LivingEntity getShooter() {
      return this.shooter;
   }

   public ItemStack getGunItemStack() {
      return this.gunItemStack;
   }

   public LogicalSide getLogicalSide() {
      return this.logicalSide;
   }

   public interface Callback {
      void post(GunFireSelectEvent var1);
   }
}
