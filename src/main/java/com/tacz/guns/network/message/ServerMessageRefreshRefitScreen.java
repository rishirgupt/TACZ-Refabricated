package com.tacz.guns.network.message;

import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
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

public class ServerMessageRefreshRefitScreen implements CustomPacketPayload {
   public static final ServerMessageRefreshRefitScreen INSTANCE = new ServerMessageRefreshRefitScreen();
   public static final Type<ServerMessageRefreshRefitScreen> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_refresh_refit_screen"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageRefreshRefitScreen> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageRefreshRefitScreen message, Context context) {
      context.client().execute(ServerMessageRefreshRefitScreen::updateScreen);
   }

   @Environment(EnvType.CLIENT)
   private static void updateScreen() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && Minecraft.getInstance().gui.screen() instanceof GunRefitScreen screen) {
         screen.init();
         AttachmentPropertyManager.postChangeEvent(player, player.getMainHandItem());
      }
   }
}
