package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ClientMessageSyncBaseTimestamp implements CustomPacketPayload {
   public static final ClientMessageSyncBaseTimestamp INSTANCE = new ClientMessageSyncBaseTimestamp();
   public static final Type<ClientMessageSyncBaseTimestamp> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_sync_base_timestamp"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessageSyncBaseTimestamp> STREAM_CODEC = StreamCodec.unit(INSTANCE);
   private static final Marker MARKER = MarkerFactory.getMarker("SYNC_BASE_TIMESTAMP");

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessageSyncBaseTimestamp message, Context context) {
      long timestamp = System.currentTimeMillis();
      context.server().execute(() -> {
         ServerPlayer entity = context.player();
         ShooterDataHolder dataHolder = IGunOperator.fromLivingEntity(entity).getDataHolder();
         dataHolder.baseTimestamp = timestamp;
         GunMod.LOGGER.debug(MARKER, "Update server base timestamp: {}", dataHolder.baseTimestamp);
      });
   }
}
