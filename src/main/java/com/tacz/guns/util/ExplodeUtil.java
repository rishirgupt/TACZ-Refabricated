package com.tacz.guns.util;

import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.util.block.ProjectileExplosion;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.phys.Vec3;

public class ExplodeUtil {
   public static void createExplosion(Entity owner, Entity exploder, float damage, float radius, boolean knockback, boolean destroy, Vec3 hitPos) {
      if (exploder.level() instanceof ServerLevel level) {
         BlockInteraction mode = destroy ? BlockInteraction.DESTROY : BlockInteraction.KEEP;
         ProjectileExplosion explosion = new ProjectileExplosion(
            level, owner, exploder, null, null, hitPos.x(), hitPos.y(), hitPos.z(), damage, radius, knockback, mode
         );
         int blocks = explosion.explode();
         int visibleDistance = AmmoConfig.EXPLOSIVE_AMMO_VISIBLE_DISTANCE.get();
         level.players()
            .stream()
            .filter(player -> Mth.sqrt((float)player.distanceToSqr(hitPos)) < visibleDistance)
            .forEach(
               player -> player.connection.send(
                  new ClientboundExplodePacket(
                     hitPos,
                     radius,
                     blocks,
                     Optional.ofNullable(explosion.getHitPlayers().get(player)),
                     ParticleTypes.EXPLOSION,
                     SoundEvents.GENERIC_EXPLODE,
                     WeightedList.of()
                  )
               )
            );
      }
   }
}
