package com.tacz.guns.debug;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public class GunMeleeDebug {
   public static void showRange(LivingEntity user, int distance, Vec3 centrePos, Vec3 eyeVec, float rangeAngle) {
      if (user.level() instanceof ServerLevel serverLevel) {
         int var17 = distance / 2;
         Vec3 startPos = user.getEyePosition().subtract(var17, var17, var17);

         for (int i = 0; i < distance * 2; i++) {
            for (int j = 0; j < distance * 2; j++) {
               for (int k = 0; k < distance * 2; k++) {
                  Vec3 tmpPos = startPos.add(i / 2.0, j / 2.0, k / 2.0);
                  Vec3 targetVec = tmpPos.subtract(centrePos);
                  double targetLength = targetVec.length();
                  if (!(targetLength < distance)) {
                     double degree = Math.toDegrees(Math.acos(targetVec.dot(eyeVec) / (targetLength * distance)));
                     if (degree < rangeAngle / 2.0F) {
                        serverLevel.sendParticles(ParticleTypes.FLAME, tmpPos.x, tmpPos.y, tmpPos.z, 1, 0.0, 0.0, 0.0, 0.0);
                     }
                  }
               }
            }
         }
      }
   }
}
