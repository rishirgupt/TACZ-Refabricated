package com.tacz.guns.network.message;

import com.tacz.guns.client.gui.GunSmithTableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ServerMessageCraft(int menuId) implements CustomPacketPayload {
   public static final Type<ServerMessageCraft> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_craft"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageCraft> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, message -> message.menuId, ServerMessageCraft::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageCraft message, Context context) {
      context.client().execute(() -> updateScreen(message.menuId));
   }

   @Environment(EnvType.CLIENT)
   private static void updateScreen(int containerId) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player.containerMenu.containerId == containerId && Minecraft.getInstance().gui.screen() instanceof GunSmithTableScreen screen) {
         screen.updateIngredientCount();
      }
   }
}
