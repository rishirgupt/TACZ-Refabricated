package com.tacz.guns.event;

import cn.sh1rocu.tacz.api.event.EntityJoinLevelEvent;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSyncBaseTimestamp;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

public class SyncBaseTimestamp {
   public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
         NetworkHandler.sendToClientPlayer(ServerMessageSyncBaseTimestamp.INSTANCE, (ServerPlayer)player);
      }
   }
}
