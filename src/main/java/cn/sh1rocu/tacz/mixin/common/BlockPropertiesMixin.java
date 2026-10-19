package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.util.RegistrationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesMixin {
   @Shadow
   private ResourceKey<Block> id;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void tacz$assignRegistrationKey(CallbackInfo ci) {
      ResourceKey<Block> key = RegistrationContext.block();
      if (key != null && this.id == null) {
         this.id = key;
      }
   }
}
