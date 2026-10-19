package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.api.event.EntityJoinLevelEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
   @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
   public void tacz$addEntityEvent(ServerPlayer serverPlayer, CallbackInfo ci) {
      EntityJoinLevelEvent event = new EntityJoinLevelEvent(serverPlayer, (ServerLevel)(Object)this);
      ((EntityJoinLevelEvent.Callback)EntityJoinLevelEvent.CALLBACK.invoker()).post(event);
      if (event.isCanceled()) {
         ci.cancel();
      }
   }
}
