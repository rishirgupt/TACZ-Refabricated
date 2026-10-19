package cn.sh1rocu.tacz.mixin.common.itemhandler;

import cn.sh1rocu.tacz.util.forge.LazyOptional;
import cn.sh1rocu.tacz.util.itemhandler.IItemHandler;
import cn.sh1rocu.tacz.util.itemhandler.InvWrapper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal {
   @Shadow
   protected SimpleContainer inventory;
   @Unique
   private LazyOptional<?> itemHandler = null;

   protected AbstractHorseMixin(EntityType<? extends Animal> entityType, Level world) {
      super(entityType, world);
   }

   @Inject(method = "createInventory", at = @At("TAIL"))
   private void createInventory(CallbackInfo ci) {
      this.itemHandler = LazyOptional.of(() -> new InvWrapper(this.inventory));
   }

   public LazyOptional<IItemHandler> tacz$getItemHandler(@Nullable Direction facing) {
      return this.isAlive() && this.itemHandler != null ? this.itemHandler.cast() : super.tacz$getItemHandler(facing);
   }

   public void tacz$invalidateItemHandler() {
      super.tacz$invalidateItemHandler();
      if (this.itemHandler != null) {
         LazyOptional<?> oldHandler = this.itemHandler;
         this.itemHandler = null;
         oldHandler.invalidate();
      }
   }
}
