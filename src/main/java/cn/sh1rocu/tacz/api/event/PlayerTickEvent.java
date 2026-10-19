package cn.sh1rocu.tacz.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

public class PlayerTickEvent extends PlayerEvent {
   public static final Event<PlayerTickEvent.Pre.Callback> START = EventFactory.createArrayBacked(PlayerTickEvent.Pre.Callback.class, callbacks -> event -> {
      for (PlayerTickEvent.Pre.Callback callback : callbacks) {
         callback.onStart(event);
      }
   });
   public static final Event<PlayerTickEvent.Post.Callback> END = EventFactory.createArrayBacked(PlayerTickEvent.Post.Callback.class, callbacks -> event -> {
      for (PlayerTickEvent.Post.Callback callback : callbacks) {
         callback.onEnd(event);
      }
   });

   protected PlayerTickEvent(Player player) {
      super(player);
   }

   public static class Post extends PlayerTickEvent {
      public Post(Player player) {
         super(player);
      }

      public interface Callback {
         void onEnd(PlayerTickEvent.Post var1);
      }
   }

   public static class Pre extends PlayerTickEvent {
      public Pre(Player player) {
         super(player);
      }

      public interface Callback {
         void onStart(PlayerTickEvent.Pre var1);
      }
   }
}
