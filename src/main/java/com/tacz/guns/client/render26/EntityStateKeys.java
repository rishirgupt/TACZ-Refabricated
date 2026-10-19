package com.tacz.guns.client.render26;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.world.entity.LivingEntity;

public final class EntityStateKeys {
   /** The living entity a render state was extracted from; model and layer hooks only receive the state. */
   public static final RenderStateDataKey<LivingEntity> ENTITY = RenderStateDataKey.create(() -> "tacz entity");

   private EntityStateKeys() {
   }
}
