package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.util.forge.EventHooks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableResourcesMixin {
   @ModifyReturnValue(method = "listeners", at = @At("RETURN"))
   private List<PreparableReloadListener> tacz$addReloadListeners(List<PreparableReloadListener> original) {
      ReloadableServerResources self = (ReloadableServerResources)(Object)this;
      List<PreparableReloadListener> listeners = new ArrayList<>(original);
      listeners.addAll(EventHooks.onResourceReload(self, self.fullRegistries().lookup()));
      return listeners;
   }
}
