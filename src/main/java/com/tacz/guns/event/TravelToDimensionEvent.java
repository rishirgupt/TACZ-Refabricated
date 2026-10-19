package com.tacz.guns.event;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TravelToDimensionEvent {
   public static void onTravelToDimension(Entity originalEntity, Entity newEntity, ServerLevel origin, ServerLevel destination) {
      if (newEntity instanceof LivingEntity livingEntity && livingEntity.getMainHandItem().getItem() instanceof IGun) {
         IGunOperator.fromLivingEntity(livingEntity).initialData();
      }
   }

   public static void onTravelToDimension(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
      if (player.getMainHandItem().getItem() instanceof IGun) {
         IGunOperator.fromLivingEntity(player).initialData();
      }
   }
}
