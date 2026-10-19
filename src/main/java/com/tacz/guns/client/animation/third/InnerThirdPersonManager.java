package com.tacz.guns.client.animation.third;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.compat.playeranimator.PlayerAnimatorCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Pose;
import net.minecraft.client.model.geom.ModelPart;

public class InnerThirdPersonManager {
   public static void setRotationAnglesHead(LivingEntity entityIn, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head, float limbSwingAmount) {
      if (!Minecraft.getInstance().isPaused()) {
         if (entityIn instanceof IGunOperator operator) {
            ItemStack mainHandItem = entityIn.getMainHandItem();
            IGun iGun = IGun.getIGunOrNull(mainHandItem);
            if (iGun == null) {
               PlayerAnimatorCompat.stopAllAnimation(entityIn);
               return;
            }

            if (entityIn.getPose() == Pose.SLEEPING
               || entityIn.onClimbable()
               || entityIn.isSwimming()
               || entityIn.getPose() == Pose.FALL_FLYING) {
               PlayerAnimatorCompat.stopAllAnimation(entityIn);
               return;
            }

            TimelessAPI.getGunDisplay(mainHandItem).ifPresent(display -> {
               if (PlayerAnimatorCompat.hasPlayerAnimator3rd(entityIn, display)) {
                  PlayerAnimatorCompat.playAnimation(entityIn, display, limbSwingAmount);
               } else {
                  playVanillaAnimation(entityIn, rightArm, leftArm, body, head, operator, display);
               }
            });
         }
      }
   }

   private static void playVanillaAnimation(
      LivingEntity entityIn, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head, IGunOperator operator, GunDisplayInstance display
   ) {
      String animation = display.getThirdPersonAnimation();
      float aimingProgress = operator.getSynAimingProgress();
      if (aimingProgress <= 0.0F) {
         ThirdPersonManager.getAnimation(animation).animateGunHold(entityIn, rightArm, leftArm, body, head);
      } else {
         ThirdPersonManager.getAnimation(animation).animateGunAim(entityIn, rightArm, leftArm, body, head, aimingProgress);
      }
   }
}
