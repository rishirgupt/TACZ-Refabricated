package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ServerMessageSyncBaseTimestamp implements CustomPacketPayload {
   public static final ServerMessageSyncBaseTimestamp INSTANCE = new ServerMessageSyncBaseTimestamp();
   public static final Type<ServerMessageSyncBaseTimestamp> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_sync_base_timestamp"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageSyncBaseTimestamp> STREAM_CODEC = StreamCodec.unit(INSTANCE);
   private static final Marker MARKER = MarkerFactory.getMarker("SYNC_BASE_TIMESTAMP");

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageSyncBaseTimestamp message, Context context) {
      long timestamp = System.currentTimeMillis();
      context.client().execute(() -> updateBaseTimestamp(timestamp));
      context.responseSender().sendPacket(ClientMessageSyncBaseTimestamp.INSTANCE);
   }

   @Environment(EnvType.CLIENT)
   private static void updateBaseTimestamp(long timestamp) {
      LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
      LocalPlayerDataHolder dataHolder = IClientPlayerGunOperator.fromLocalPlayer(player).getDataHolder();
      dataHolder.clientBaseTimestamp = timestamp;
      GunMod.LOGGER.debug(MARKER, "Update client base timestamp: {}", dataHolder.clientBaseTimestamp);
   }
}
