package com.tacz.guns.init;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.Holder.Reference;

public class ModDamageTypes {
   public static final ResourceKey<DamageType> BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("tacz", "bullet"));
   public static final ResourceKey<DamageType> BULLET_IGNORE_ARMOR = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("tacz", "bullet_ignore_armor")
   );
   public static final ResourceKey<DamageType> BULLET_VOID = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("tacz", "bullet_void"));
   public static final ResourceKey<DamageType> BULLET_VOID_IGNORE_ARMOR = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("tacz", "bullet_void_ignore_armor")
   );
   public static final TagKey<DamageType> BULLETS_TAG = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("tacz", "bullets"));

   public static class Sources {
      private static Reference<DamageType> getHolder(RegistryAccess access, ResourceKey<DamageType> damageTypeKey) {
         return access.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(damageTypeKey);
      }

      public static DamageSource bullet(RegistryAccess access, Entity bullet, Entity shooter, boolean ignoreArmor) {
         return new DamageSource(getHolder(access, ignoreArmor ? ModDamageTypes.BULLET_IGNORE_ARMOR : ModDamageTypes.BULLET), bullet, shooter);
      }

      public static DamageSource bulletVoid(RegistryAccess access, Entity bullet, Entity shooter, boolean ignoreArmor) {
         return new DamageSource(getHolder(access, ignoreArmor ? ModDamageTypes.BULLET_VOID_IGNORE_ARMOR : ModDamageTypes.BULLET_VOID), bullet, shooter);
      }
   }
}
