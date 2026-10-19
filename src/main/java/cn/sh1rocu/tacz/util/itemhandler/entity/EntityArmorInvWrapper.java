package cn.sh1rocu.tacz.util.itemhandler.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;

public class EntityArmorInvWrapper extends EntityEquipmentInvWrapper {
   public EntityArmorInvWrapper(LivingEntity entity) {
      super(entity, Type.HUMANOID_ARMOR);
   }
}
