package com.tacz.guns.mixin.client;

import com.tacz.guns.client.animation.third.InnerThirdPersonManager;
import com.tacz.guns.client.render26.EntityStateKeys;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
   @Shadow
   @Final
   public ModelPart head;
   @Shadow
   @Final
   public ModelPart body;
   @Shadow
   @Final
   public ModelPart leftArm;
   @Shadow
   @Final
   public ModelPart rightArm;

   @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
   private void tacz$setRotationAnglesHead(HumanoidRenderState state, CallbackInfo ci) {
      LivingEntity entity = state.getData(EntityStateKeys.ENTITY);
      if (entity != null && state.ageInTicks != 0.0F) {
         InnerThirdPersonManager.setRotationAnglesHead(entity, this.rightArm, this.leftArm, this.body, this.head, state.walkAnimationSpeed);
      }
   }
}
