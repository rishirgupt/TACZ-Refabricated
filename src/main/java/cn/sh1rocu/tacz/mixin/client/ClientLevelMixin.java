package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.event.EntityJoinLevelEvent;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.RegistryAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
   @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
   public void tacz$entityJoinLevelEvent(Entity entityToSpawn, CallbackInfo ci) {
      EntityJoinLevelEvent event = new EntityJoinLevelEvent(entityToSpawn, (net.minecraft.client.multiplayer.ClientLevel)(Object)this);
      ((EntityJoinLevelEvent.Callback)EntityJoinLevelEvent.CALLBACK.invoker()).post(event);
      if (event.isCanceled()) {
         ci.cancel();
      }
   }
}
