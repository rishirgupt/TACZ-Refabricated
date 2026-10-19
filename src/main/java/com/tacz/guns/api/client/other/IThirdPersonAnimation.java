package com.tacz.guns.api.client.other;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.model.geom.ModelPart;

public interface IThirdPersonAnimation {
   void animateGunHold(LivingEntity var1, ModelPart var2, ModelPart var3, ModelPart var4, ModelPart var5);

   void animateGunAim(LivingEntity var1, ModelPart var2, ModelPart var3, ModelPart var4, ModelPart var5, float var6);
}
