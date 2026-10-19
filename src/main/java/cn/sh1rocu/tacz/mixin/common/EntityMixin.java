package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.api.event.EntityRemoveEvent;
import cn.sh1rocu.tacz.api.extension.IEntityPersistentData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements IEntityPersistentData {
   @Shadow
   private Level level;
   @Unique
   private CompoundTag tacz$persistentData;

   @Inject(method = "remove", at = @At("TAIL"))
   private void remove(RemovalReason reason, CallbackInfo ci) {
      if (!this.level.isClientSide()) {
         EntityRemoveEvent event = new EntityRemoveEvent((Entity)(Object)this);
         ((EntityRemoveEvent.Callback)EntityRemoveEvent.EVENT.invoker()).onEntityRemove(event);
      }
   }

   @Unique
   @Override
   public CompoundTag tacz$getPersistentData() {
      if (this.tacz$persistentData == null) {
         this.tacz$persistentData = new CompoundTag();
      }

      return this.tacz$persistentData;
   }

   @Inject(
      method = "saveWithoutId",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V")
   )
   private void tacz$savePersistentData(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
      if (this.tacz$persistentData != null) {
         output.store("NeoForgeData", CompoundTag.CODEC, this.tacz$persistentData.copy());
      }
   }

   @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"))
   private void tacz$loadPersistentData(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
      input.read("NeoForgeData", CompoundTag.CODEC).ifPresent(data -> this.tacz$persistentData = data);
   }
}
