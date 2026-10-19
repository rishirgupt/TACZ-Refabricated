package cn.sh1rocu.tacz.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

public class PlayerEvent extends LivingEvent {
   private final Player player;
   public static final Event<PlayerEvent.PlayerLoggedInCallback> LOGGED_IN = EventFactory.createArrayBacked(
      PlayerEvent.PlayerLoggedInCallback.class, callbacks -> event -> {
         for (PlayerEvent.PlayerLoggedInCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );
   public static final Event<PlayerEvent.PlayerLoggedOutCallback> LOGGED_OUT = EventFactory.createArrayBacked(
      PlayerEvent.PlayerLoggedOutCallback.class, callbacks -> event -> {
         for (PlayerEvent.PlayerLoggedOutCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public PlayerEvent(Player player) {
      super(player);
      this.player = player;
   }

   public Player getEntity() {
      return this.player;
   }

   public interface PlayerLoggedInCallback {
      void post(PlayerEvent.PlayerLoggedInEvent var1);
   }

   public static class PlayerLoggedInEvent extends PlayerEvent {
      public PlayerLoggedInEvent(Player player) {
         super(player);
      }
   }

   public interface PlayerLoggedOutCallback {
      void post(PlayerEvent.PlayerLoggedOutEvent var1);
   }

   public static class PlayerLoggedOutEvent extends PlayerEvent {
      public PlayerLoggedOutEvent(Player player) {
         super(player);
      }
   }
}
