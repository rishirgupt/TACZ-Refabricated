package cn.sh1rocu.tacz.mixin.common.itemhandler;

import cn.sh1rocu.tacz.api.mixin.ItemHandlerCapability;
import cn.sh1rocu.tacz.util.forge.LazyOptional;
import cn.sh1rocu.tacz.util.itemhandler.IItemHandler;
import cn.sh1rocu.tacz.util.itemhandler.entity.EntityEquipmentInvWrapper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ItemHandlerCapability {
   @Unique
   private LazyOptional<?>[] handlers;

   @Shadow
   public abstract boolean isAlive();

   @Inject(method = "<init>", at = @At("TAIL"))
   private void initClass(EntityType<? extends LivingEntity> entityType, Level world, CallbackInfo ci) {
      this.handlers = EntityEquipmentInvWrapper.create(this.self());
   }

   @Override
   public LazyOptional<IItemHandler> tacz$getItemHandler(@Nullable Direction facing) {
      if (this.isAlive()) {
         if (facing == null) {
            return this.handlers[2].cast();
         }

         if (facing.getAxis().isVertical()) {
            return this.handlers[0].cast();
         }

         if (facing.getAxis().isHorizontal()) {
            return this.handlers[1].cast();
         }
      }

      return LazyOptional.empty();
   }

   @Override
   public void tacz$invalidateItemHandler() {
      for (LazyOptional<?> handler : this.handlers) {
         handler.invalidate();
      }
   }

   @Override
   public void tacz$reviveItemHandler() {
      this.handlers = EntityEquipmentInvWrapper.create(this.self());
   }

   @Unique
   private LivingEntity self() {
      return (LivingEntity)(Object)this;
   }
}
