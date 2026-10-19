package com.tacz.guns.init;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.TargetMinecart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModEntities {
   public static EntityType<EntityKineticBullet> BULLET = register("bullet", EntityKineticBullet.TYPE);
   public static EntityType<TargetMinecart> TARGET_MINECART = register("target_minecart", TargetMinecart.TYPE);

   public static void init() {
   }

   private static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
      return (EntityType<T>)Registry.register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("tacz", name), type);
   }
}
