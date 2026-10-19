package cn.sh1rocu.tacz.mixin.compat.controllable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.compat.controllable.ControllableInner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = BindingContext.class, remap = false)
public abstract class BindingContextMixin {
   @ModifyReturnValue(method = "isActive", at = @At("RETURN"))
   private boolean tacz$controllableCompat(boolean original) {
      BindingContext instance = BindingContext.class.cast(this);
      if (!(instance instanceof ControllableInner.GunKeyConflict)) {
         return original;
      }

      LocalPlayer player = Minecraft.getInstance().player;
      return player == null ? original : original && IGun.mainHandHoldGun(player);
   }
}
