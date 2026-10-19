package com.tacz.guns.event;

import cn.sh1rocu.tacz.api.event.LivingHurtEvent;
import com.tacz.guns.init.ModAttributes;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntityDamageEvent {
   public static void onLivingHurt(LivingHurtEvent event) {
      if (event.getSource().is(ModDamageTypes.BULLETS_TAG)) {
         LivingEntity living = event.getEntity();
         AttributeInstance resistance = living.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ModAttributes.BULLET_RESISTANCE));
         if (resistance != null) {
            float modifiedDamage = event.getAmount() * (float)(1.0 - resistance.getValue());
            event.setAmount(modifiedDamage);
         }
      }
   }
}
