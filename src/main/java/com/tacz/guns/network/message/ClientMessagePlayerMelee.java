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

public class ClientMessagePlayerMelee implements CustomPacketPayload {
   public static final ClientMessagePlayerMelee INSTANCE = new ClientMessagePlayerMelee();
   public static final Type<ClientMessagePlayerMelee> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_player_melee"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessagePlayerMelee> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessagePlayerMelee message, Context context) {
      context.server().execute(() -> {
         ServerPlayer entity = context.player();
         IGunOperator.fromLivingEntity(entity).melee();
      });
   }
}
