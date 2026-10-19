package com.tacz.guns.network.message;

import com.tacz.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public class ClientMessagePlayerZoom implements CustomPacketPayload {
   public static final ClientMessagePlayerZoom INSTANCE = new ClientMessagePlayerZoom();
   public static final Type<ClientMessagePlayerZoom> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_player_zoom"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessagePlayerZoom> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessagePlayerZoom message, Context context) {
      context.server().execute(() -> {
         ServerPlayer entity = context.player();
         IGunOperator.fromLivingEntity(entity).zoom();
      });
   }
}
