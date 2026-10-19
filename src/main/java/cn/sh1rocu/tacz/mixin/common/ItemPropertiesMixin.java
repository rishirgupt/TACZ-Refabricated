package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.util.RegistrationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {
   @Shadow
   private ResourceKey<Item> id;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void tacz$assignRegistrationKey(CallbackInfo ci) {
      ResourceKey<Item> key = RegistrationContext.item();
      if (key != null && this.id == null) {
         this.id = key;
      }
   }
}
