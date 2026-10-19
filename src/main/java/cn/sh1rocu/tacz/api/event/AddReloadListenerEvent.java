package cn.sh1rocu.tacz.api.event;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;

public class AddReloadListenerEvent extends BaseEvent {
   private final List<PreparableReloadListener> listeners = new ArrayList<>();
   private final ReloadableServerResources serverResources;
   private final net.minecraft.core.HolderLookup.Provider registryAccess;
   public static Event<AddReloadListenerEvent.Callback> CALLBACK = EventFactory.createArrayBacked(
      AddReloadListenerEvent.Callback.class, callbacks -> event -> {
         for (AddReloadListenerEvent.Callback e : callbacks) {
            e.post(event);
         }
      }
   );

   public AddReloadListenerEvent(ReloadableServerResources serverResources, net.minecraft.core.HolderLookup.Provider registryAccess) {
      this.serverResources = serverResources;
      this.registryAccess = registryAccess;
   }

   public ReloadableServerResources getServerResources() {
      return this.serverResources;
   }

   public net.minecraft.core.HolderLookup.Provider getRegistryAccess() {
      return this.registryAccess;
   }

   public void addListener(PreparableReloadListener listener) {
      this.listeners.add(new AddReloadListenerEvent.WrappedStateAwareListener(listener));
   }

   public List<PreparableReloadListener> getListeners() {
      return ImmutableList.copyOf(this.listeners);
   }

   public interface Callback {
      void post(AddReloadListenerEvent var1);
   }

   private static class WrappedStateAwareListener implements PreparableReloadListener {
      private final PreparableReloadListener wrapped;

      private WrappedStateAwareListener(PreparableReloadListener wrapped) {
         this.wrapped = wrapped;
      }

      @Override
      public CompletableFuture<Void> reload(
         PreparableReloadListener.SharedState sharedState, Executor backgroundExecutor, PreparationBarrier barrier, Executor gameExecutor
      ) {
         return FabricLoader.getInstance().isModLoaded("tacz")
            ? this.wrapped.reload(sharedState, backgroundExecutor, barrier, gameExecutor)
            : CompletableFuture.completedFuture(null);
      }
   }
}
