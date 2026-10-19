package com.tacz.guns.network.message.handshake;

import com.tacz.guns.GunMod;
import com.tacz.guns.network.IHandshakeMessage;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking.Context;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class AcknowledgeC2SPacket implements IHandshakeMessage.IResponsePacket {
   public static final AcknowledgeC2SPacket INSTANCE = new AcknowledgeC2SPacket();
   public static final Marker ACKNOWLEDGE = MarkerFactory.getMarker("HANDSHAKE_ACKNOWLEDGE");
   public static final Type<AcknowledgeC2SPacket> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "acknowledge"));
   public static final StreamCodec<FriendlyByteBuf, AcknowledgeC2SPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   @NotNull
   public Type<AcknowledgeC2SPacket> type() {
      return TYPE;
   }

   @Override
   public void handle(Context context) {
      GunMod.LOGGER.debug(ACKNOWLEDGE, "Received acknowledgement from client");
   }
}
