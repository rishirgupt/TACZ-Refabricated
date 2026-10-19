package com.tacz.guns.mixin.client;

import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public class AbstractButtonMixin {
   @Inject(method = "onClick(Lnet/minecraft/client/input/MouseButtonEvent;Z)V", at = @At("HEAD"))
   public void onClickHead(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick, CallbackInfo ci) {
      LocalPlayerDataHolder.clientClickButtonTimestamp = System.currentTimeMillis();
   }
}
