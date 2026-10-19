package cn.sh1rocu.tacz.util.itemhandler.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;

public class EntityHandsInvWrapper extends EntityEquipmentInvWrapper {
   public EntityHandsInvWrapper(LivingEntity entity) {
      super(entity, Type.HAND);
   }
}
