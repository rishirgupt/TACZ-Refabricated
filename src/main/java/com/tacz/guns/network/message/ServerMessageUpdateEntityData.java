package com.tacz.guns.network.message;

import com.tacz.guns.entity.sync.core.DataEntry;
import com.tacz.guns.entity.sync.core.SyncedEntityData;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ServerMessageUpdateEntityData(int entityId, List<DataEntry<?, ?>> entries) implements CustomPacketPayload {
   public static final Type<ServerMessageUpdateEntityData> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_update_entity_data"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageUpdateEntityData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      message -> message.entityId,
      DataEntry.STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)),
      message -> message.entries,
      ServerMessageUpdateEntityData::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageUpdateEntityData message, Context context) {
      context.client().execute(() -> onHandle(message));
   }

   @Environment(EnvType.CLIENT)
   private static void onHandle(ServerMessageUpdateEntityData message) {
      Level level = Minecraft.getInstance().level;
      if (level != null) {
         Entity entity = level.getEntity(message.entityId);
         if (entity != null) {
            SyncedEntityData instance = SyncedEntityData.instance();
            message.entries.forEach(entry -> instance.set(entity, entry.getKey(), entry.getValue()));
         }
      }
   }
}
