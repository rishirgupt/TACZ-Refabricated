package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.event.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
   @Shadow
   @Final
   private Minecraft minecraft;

   @Inject(
      method = "onButton",
      at = @At("HEAD"),
      cancellable = true
   )
   private void tacz$onMouseButtonPre(long windowPointer, net.minecraft.client.input.MouseButtonInfo info, int action, CallbackInfo ci) {
      InputEvent.MouseButton.Pre event = new InputEvent.MouseButton.Pre(info.button(), action, info.modifiers());
      ((InputEvent.MousePreCallback)InputEvent.MouseButton.Pre.EVENT.invoker()).onMousePre(event);
      if (event.isCanceled()) {
         ci.cancel();
      }
   }

   @Inject(method = "onButton", at = @At("TAIL"))
   private void tacz$onMouseButtonPost(long windowPointer, net.minecraft.client.input.MouseButtonInfo info, int action, CallbackInfo ci) {
      if (windowPointer == this.minecraft.getWindow().handle()) {
         InputEvent.MouseButton.Post event = new InputEvent.MouseButton.Post(info.button(), action, info.modifiers());
         ((InputEvent.MousePostCallback)InputEvent.MouseButton.Post.EVENT.invoker()).onMousePost(event);
      }
   }
}
