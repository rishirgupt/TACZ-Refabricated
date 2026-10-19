package com.tacz.guns.network.message;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ServerMessageLevelUp(ItemStack gun, int level) implements CustomPacketPayload {
   public static final Type<ServerMessageLevelUp> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_level_up"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageLevelUp> STREAM_CODEC = StreamCodec.composite(
      ItemStack.STREAM_CODEC, ServerMessageLevelUp::getGun, ByteBufCodecs.INT, ServerMessageLevelUp::getLevel, ServerMessageLevelUp::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageLevelUp message, Context context) {
      context.client().execute(() -> onLevelUp(message));
   }

   @Environment(EnvType.CLIENT)
   private static void onLevelUp(ServerMessageLevelUp message) {
      int level = message.getLevel();
      ItemStack gun = message.getGun();
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         ;
      }
   }

   public ItemStack getGun() {
      return this.gun;
   }

   public int getLevel() {
      return this.level;
   }
}
