package com.tacz.guns.util;

import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import com.tacz.guns.entity.EntityKineticBullet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityUtil {
   private static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();

   @Nullable
   public static EntityKineticBullet.EntityResult findEntityOnPath(Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {
      Vec3 hitVec = null;
      Entity hitEntity = null;
      boolean headshot = false;
      List<Entity> entities = bulletEntity.level()
         .getEntities(bulletEntity, bulletEntity.getBoundingBox().expandTowards(bulletEntity.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
      double closestDistance = Double.MAX_VALUE;
      Entity owner = bulletEntity.getOwner();

      for (Entity entity : entities) {
         if (!entity.equals(owner) && (owner == null || !entity.isPassengerOfSameVehicle(owner))) {
            EntityKineticBullet.EntityResult result = getHitResult(bulletEntity, entity, startVec, endVec);
            if (result != null) {
               Vec3 hitPos = result.getHitPos();
               double distanceToHit = startVec.distanceTo(hitPos);
               if (entity.isAlive() && distanceToHit < closestDistance) {
                  hitVec = hitPos;
                  hitEntity = entity;
                  closestDistance = distanceToHit;
                  headshot = result.isHeadshot();
               }
            }
         }
      }

      return hitEntity != null ? new EntityKineticBullet.EntityResult(hitEntity, hitVec, headshot) : null;
   }

   @NotNull
   public static List<EntityKineticBullet.EntityResult> findEntitiesOnPath(Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {
      List<EntityKineticBullet.EntityResult> hitEntities = new ArrayList<>();
      List<Entity> entities = bulletEntity.level()
         .getEntities(bulletEntity, bulletEntity.getBoundingBox().expandTowards(bulletEntity.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
      Entity owner = bulletEntity.getOwner();

      for (Entity entity : entities) {
         if (!entity.equals(owner) && (owner == null || !entity.equals(owner.getVehicle()))) {
            EntityKineticBullet.EntityResult result = getHitResult(bulletEntity, entity, startVec, endVec);
            if (result != null && entity.isAlive()) {
               hitEntities.add(result);
            }
         }
      }

      return hitEntities;
   }

   @Nullable
   protected static EntityKineticBullet.EntityResult getHitResult(Projectile bulletEntity, Entity entity, Vec3 startVec, Vec3 endVec) {
      AABB boundingBox = HitboxHelper.getFixedBoundingBox(entity, bulletEntity.getOwner());
      Vec3 hitPos = (Vec3)boundingBox.clip(startVec, endVec).orElse(null);
      if (hitPos == null) {
         return null;
      }

      Vec3 hitBoxPos = hitPos.subtract(entity.position());
      Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      if (entityId != null) {
         AABB aabb = HeadShotAABBConfigRead.getAABB(entityId);
         if (aabb != null) {
            return new EntityKineticBullet.EntityResult(entity, hitPos, aabb.contains(hitBoxPos));
         }
      }

      boolean headshot = false;
      float eyeHeight = entity.getEyeHeight();
      if (eyeHeight - 0.25 < hitBoxPos.y && hitBoxPos.y < eyeHeight + 0.25) {
         headshot = true;
      }

      return new EntityKineticBullet.EntityResult(entity, hitPos, headshot);
   }
}
