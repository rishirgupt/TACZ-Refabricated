package com.tacz.guns.util.block;

import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.util.HitboxHelper;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * An explosion from an explosive bullet. Blocks are handled like a normal explosion; entities are hurt by
 * {@code power} and pushed by the bullet's own knockback rules, using the shooter-aware hitboxes.
 */
public class ProjectileExplosion extends ServerExplosion {
   private final ServerLevel level;
   private final double x;
   private final double y;
   private final double z;
   private final float power;
   private final float radius;
   private final boolean knockback;
   private final Entity owner;
   private final Entity exploder;
   private final boolean fire;

   public ProjectileExplosion(
      ServerLevel level,
      Entity owner,
      Entity exploder,
      @Nullable DamageSource source,
      @Nullable ExplosionDamageCalculator damageCalculator,
      double x,
      double y,
      double z,
      float power,
      float radius,
      boolean knockback,
      BlockInteraction mode
   ) {
      super(level, exploder, source, damageCalculator, new Vec3(x, y, z), radius, AmmoConfig.EXPLOSIVE_AMMO_FIRE.get(), mode);
      this.level = level;
      this.x = x;
      this.y = y;
      this.z = z;
      this.power = power;
      this.radius = radius;
      this.owner = owner;
      this.exploder = exploder;
      this.knockback = knockback;
      this.fire = AmmoConfig.EXPLOSIVE_AMMO_FIRE.get();
   }

   /** Hits entities and blocks; returns how many blocks were affected. */
   @Override
   public int explode() {
      this.level.gameEvent(this.exploder, GameEvent.EXPLODE, BlockPos.containing(this.x, this.y, this.z));
      List<BlockPos> blocks = this.calculateExplodedPositions();
      this.hurtEntitiesAround();
      if (this.interactsWithBlocks()) {
         this.interactWithBlocks(blocks);
      }

      if (this.fire) {
         this.createFire(blocks);
      }

      return blocks.size();
   }

   private void hurtEntitiesAround() {
      float radius = this.radius;
      int minX = Mth.floor(this.x - radius - 1.0);
      int maxX = Mth.floor(this.x + radius + 1.0);
      int minY = Mth.floor(this.y - radius - 1.0);
      int maxY = Mth.floor(this.y + radius + 1.0);
      int minZ = Mth.floor(this.z - radius - 1.0);
      int maxZ = Mth.floor(this.z + radius + 1.0);
      radius *= 2.0F;
      List<Entity> entities = this.level.getEntities(this.exploder, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
      Vec3 explosionPos = new Vec3(this.x, this.y, this.z);

      for (Entity entity : entities) {
         if (entity.ignoreExplosion(this)) {
            continue;
         }

         AABB boundingBox = HitboxHelper.getFixedBoundingBox(entity, this.owner);
         double minDistance = radius;
         Vec3[] d = new Vec3[15];
         double strength;
         double deltaX;
         double deltaY;
         double deltaZ;
         if (!(entity instanceof LivingEntity)) {
            strength = Math.sqrt(entity.distanceToSqr(explosionPos)) * 2.0 / radius;
            deltaX = entity.getX() - this.x;
            deltaY = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
            deltaZ = entity.getZ() - this.z;
         } else {
            deltaX = (boundingBox.maxX + boundingBox.minX) / 2.0;
            deltaY = (boundingBox.maxY + boundingBox.minY) / 2.0;
            deltaZ = (boundingBox.maxZ + boundingBox.minZ) / 2.0;
            d[0] = new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            d[1] = new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            d[2] = new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            d[3] = new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            d[4] = new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            d[5] = new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            d[6] = new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            d[7] = new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            d[8] = new Vec3(boundingBox.minX, deltaY, deltaZ);
            d[9] = new Vec3(boundingBox.maxX, deltaY, deltaZ);
            d[10] = new Vec3(deltaX, boundingBox.minY, deltaZ);
            d[11] = new Vec3(deltaX, boundingBox.maxY, deltaZ);
            d[12] = new Vec3(deltaX, deltaY, boundingBox.minZ);
            d[13] = new Vec3(deltaX, deltaY, boundingBox.maxZ);
            d[14] = new Vec3(deltaX, deltaY, deltaZ);

            for (int s = 0; s < 15; s++) {
               BlockHitResult result = BlockRayTrace.rayTraceBlocks(
                  this.level, new ClipContext(explosionPos, d[s], ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())
               );
               minDistance = result.getType() != HitResult.Type.BLOCK ? Math.min(minDistance, explosionPos.distanceTo(d[s])) : minDistance;
            }

            strength = minDistance * 2.0 / radius;
            deltaX -= this.x;
            deltaY -= this.y;
            deltaZ -= this.z;
         }

         if (strength > 1.0) {
            continue;
         }

         double distanceToExplosion = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
         if (distanceToExplosion != 0.0) {
            deltaX /= distanceToExplosion;
            deltaY /= distanceToExplosion;
            deltaZ /= distanceToExplosion;
         }

         double damage = 1.0 - strength;
         entity.hurtServer(this.level, this.getDamageSource(), (float)damage * this.power);
         if (entity instanceof LivingEntity livingEntity) {
            damage *= 1.0 - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
         }

         float multiplier = this.power * radius / 500.0F;
         if (AmmoConfig.EXPLOSIVE_AMMO_KNOCK_BACK.get() && this.knockback) {
            Vec3 push = new Vec3(deltaX * damage * multiplier, deltaY * damage * multiplier, deltaZ * damage * multiplier);
            entity.setDeltaMovement(entity.getDeltaMovement().add(push));
            if (entity instanceof Player player && !player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
               this.getHitPlayers().put(player, push);
            }
         }
      }
   }
}
