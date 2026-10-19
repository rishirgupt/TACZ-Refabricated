package cn.sh1rocu.tacz.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.Connection;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public abstract class ClientPlayerNetworkEvent extends BaseEvent {
   private final MultiPlayerGameMode multiPlayerGameMode;
   private final LocalPlayer player;
   private final Connection connection;
   public static final Event<ClientPlayerNetworkEvent.LoggingOutCallback> LOGGING_OUT = EventFactory.createArrayBacked(
      ClientPlayerNetworkEvent.LoggingOutCallback.class, callbacks -> event -> {
         for (ClientPlayerNetworkEvent.LoggingOutCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );
   public static final Event<ClientPlayerNetworkEvent.CloneCallback> CLONE = EventFactory.createArrayBacked(
      ClientPlayerNetworkEvent.CloneCallback.class, callbacks -> event -> {
         for (ClientPlayerNetworkEvent.CloneCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   protected ClientPlayerNetworkEvent(MultiPlayerGameMode multiPlayerGameMode, LocalPlayer player, Connection connection) {
      this.multiPlayerGameMode = multiPlayerGameMode;
      this.player = player;
      this.connection = connection;
   }

   public MultiPlayerGameMode getMultiPlayerGameMode() {
      return this.multiPlayerGameMode;
   }

   public LocalPlayer getPlayer() {
      return this.player;
   }

   public Connection getConnection() {
      return this.connection;
   }

   public static class Clone extends ClientPlayerNetworkEvent {
      private final LocalPlayer oldPlayer;

      public Clone(MultiPlayerGameMode pc, LocalPlayer oldPlayer, LocalPlayer newPlayer, Connection networkManager) {
         super(pc, newPlayer, networkManager);
         this.oldPlayer = oldPlayer;
      }

      public LocalPlayer getOldPlayer() {
         return this.oldPlayer;
      }

      public LocalPlayer getNewPlayer() {
         return super.getPlayer();
      }

      @Override
      public LocalPlayer getPlayer() {
         return super.getPlayer();
      }
   }

   public interface CloneCallback {
      void post(ClientPlayerNetworkEvent.Clone var1);
   }

   public static class LoggingOut extends ClientPlayerNetworkEvent {
      public LoggingOut(@Nullable MultiPlayerGameMode controller, @Nullable LocalPlayer player, @Nullable Connection networkManager) {
         super(controller, player, networkManager);
      }

      @Nullable
      @Override
      public MultiPlayerGameMode getMultiPlayerGameMode() {
         return super.getMultiPlayerGameMode();
      }

      @Nullable
      @Override
      public LocalPlayer getPlayer() {
         return super.getPlayer();
      }

      @Nullable
      @Override
      public Connection getConnection() {
         return super.getConnection();
      }
   }

   public interface LoggingOutCallback {
      void post(ClientPlayerNetworkEvent.LoggingOut var1);
   }
}
