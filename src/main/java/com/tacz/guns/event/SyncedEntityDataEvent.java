package com.tacz.guns.event;

import cn.sh1rocu.tacz.api.event.EntityJoinLevelEvent;
import com.tacz.guns.entity.sync.core.DataEntry;
import com.tacz.guns.entity.sync.core.DataHolder;
import com.tacz.guns.entity.sync.core.SyncedDataKey;
import com.tacz.guns.entity.sync.core.SyncedEntityData;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageUpdateEntityData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

public final class SyncedEntityDataEvent {
   public static void onStartTracking(Entity entity, ServerPlayer player) {
      if (!player.level().isClientSide()) {
         DataHolder holder = SyncedEntityData.instance().getDataHolder(entity);
         if (holder != null) {
            List<DataEntry<?, ?>> entries = holder.gatherAll();
            entries.removeIf(entry -> !entry.getKey().syncMode().isTracking());
            if (!entries.isEmpty()) {
               NetworkHandler.sendToClientPlayer(new ServerMessageUpdateEntityData(entity.getId(), entries), player);
            }
         }
      }
   }

   public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
         DataHolder holder = SyncedEntityData.instance().getDataHolder(player);
         if (holder != null) {
            List<DataEntry<?, ?>> entries = holder.gatherAll();
            if (!entries.isEmpty()) {
               NetworkHandler.sendToClientPlayer(new ServerMessageUpdateEntityData(player.getId(), entries), (ServerPlayer)player);
            }
         }
      }
   }

   public static void onPlayerClone(ServerPlayer original, ServerPlayer player, boolean alive) {
      original.tacz$reviveItemHandler();
      DataHolder oldHolder = SyncedEntityData.instance().getDataHolder(original);
      if (oldHolder != null) {
         original.tacz$invalidateItemHandler();
         DataHolder newHolder = SyncedEntityData.instance().getDataHolder(player);
         if (newHolder != null) {
            Map<SyncedDataKey<?, ?>, DataEntry<?, ?>> dataMap = new HashMap<>(oldHolder.dataMap);
            if (!alive) {
               dataMap.entrySet().removeIf(entry -> !entry.getKey().persistent());
            }

            newHolder.dataMap = dataMap;
         }
      }
   }

   public static void onServerTick(MinecraftServer server) {
      SyncedEntityData instance = SyncedEntityData.instance();
      if (instance.isDirty()) {
         List<Entity> dirtyEntities = instance.getDirtyEntities();
         if (dirtyEntities.isEmpty()) {
            instance.setDirty(false);
         } else {
            for (Entity entity : dirtyEntities) {
               DataHolder holder = instance.getDataHolder(entity);
               if (holder != null && holder.isDirty()) {
                  List<DataEntry<?, ?>> entries = holder.gatherDirty();
                  if (!entries.isEmpty()) {
                     List<DataEntry<?, ?>> selfEntries = entries.stream().filter(entry -> entry.getKey().syncMode().isSelf()).collect(Collectors.toList());
                     if (!selfEntries.isEmpty() && entity instanceof ServerPlayer) {
                        NetworkHandler.sendToClientPlayer(new ServerMessageUpdateEntityData(entity.getId(), selfEntries), (ServerPlayer)entity);
                     }

                     List<DataEntry<?, ?>> trackingEntries = entries.stream()
                        .filter(entry -> entry.getKey().syncMode().isTracking())
                        .collect(Collectors.toList());
                     if (!trackingEntries.isEmpty()) {
                        NetworkHandler.sendToTrackingEntity(new ServerMessageUpdateEntityData(entity.getId(), trackingEntries), entity);
                     }

                     holder.clean();
                  }
               }
            }

            dirtyEntities.clear();
            instance.setDirty(false);
         }
      }
   }
}
