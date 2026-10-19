package com.tacz.guns.network.message;

import com.tacz.guns.inventory.GunSmithTableMenu;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ClientMessageCraft(Identifier recipeId, int menuId) implements CustomPacketPayload {
   public static final Type<ClientMessageCraft> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_craft"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessageCraft> STREAM_CODEC = StreamCodec.composite(
      Identifier.STREAM_CODEC, message -> message.recipeId, ByteBufCodecs.INT, message -> message.menuId, ClientMessageCraft::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessageCraft message, Context context) {
      context.server().execute(() -> {
         ServerPlayer entity = context.player();
         if (entity.containerMenu.containerId == message.menuId && entity.containerMenu instanceof GunSmithTableMenu menu) {
            menu.doCraft(message.recipeId, entity);
         }
      });
   }
}
