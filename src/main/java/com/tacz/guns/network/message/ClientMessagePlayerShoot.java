package com.tacz.guns.network.message;

import com.tacz.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ClientMessagePlayerShoot(long timestamp, float chargeProgress) implements CustomPacketPayload {
   public static final Type<ClientMessagePlayerShoot> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_player_shoot"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessagePlayerShoot> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_LONG, message -> message.timestamp, ByteBufCodecs.FLOAT, message -> message.chargeProgress, ClientMessagePlayerShoot::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessagePlayerShoot message, Context context) {
      context.server().execute(() -> {
         ServerPlayer entity = context.player();
         IGunOperator.fromLivingEntity(entity).shoot(entity::getXRot, entity::getYRot, message.timestamp, message.chargeProgress);
      });
   }
}
