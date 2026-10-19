package cn.sh1rocu.tacz.util.forge;

import cn.sh1rocu.tacz.api.event.AddReloadListenerEvent;
import cn.sh1rocu.tacz.api.event.PlayerEvent;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.core.RegistryAccess;

public class EventHooks {
   public static void firePlayerLoggedIn(Player player) {
      ((PlayerEvent.PlayerLoggedInCallback)PlayerEvent.LOGGED_IN.invoker()).post(new PlayerEvent.PlayerLoggedInEvent(player));
   }

   public static void firePlayerLoggedOut(Player player) {
      ((PlayerEvent.PlayerLoggedOutCallback)PlayerEvent.LOGGED_OUT.invoker()).post(new PlayerEvent.PlayerLoggedOutEvent(player));
   }

   public static List<PreparableReloadListener> onResourceReload(ReloadableServerResources serverResources, net.minecraft.core.HolderLookup.Provider registryAccess) {
      AddReloadListenerEvent event = new AddReloadListenerEvent(serverResources, registryAccess);
      ((AddReloadListenerEvent.Callback)AddReloadListenerEvent.CALLBACK.invoker()).post(event);
      return event.getListeners();
   }
}
