package com.tacz.guns.network.message;

import com.tacz.guns.api.client.event.SwapItemWithOffHand;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public class ServerMessageSwapItem implements CustomPacketPayload {
   public static final ServerMessageSwapItem INSTANCE = new ServerMessageSwapItem();
   public static final Type<ServerMessageSwapItem> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_swap_item"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageSwapItem> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageSwapItem message, Context context) {
      ((SwapItemWithOffHand.Callback)SwapItemWithOffHand.CALLBACK.invoker()).post(new SwapItemWithOffHand());
   }
}
