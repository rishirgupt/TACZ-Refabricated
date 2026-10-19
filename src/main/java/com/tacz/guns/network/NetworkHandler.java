package com.tacz.guns.network;

import cn.sh1rocu.tacz.util.forge.network.AdvancedAddEntityPayload;
import com.tacz.guns.network.message.ClientMessageCraft;
import com.tacz.guns.network.message.ClientMessageLaserColor;
import com.tacz.guns.network.message.ClientMessagePlayerAim;
import com.tacz.guns.network.message.ClientMessagePlayerBoltGun;
import com.tacz.guns.network.message.ClientMessagePlayerCancelReload;
import com.tacz.guns.network.message.ClientMessagePlayerCrawl;
import com.tacz.guns.network.message.ClientMessagePlayerDrawGun;
import com.tacz.guns.network.message.ClientMessagePlayerFireSelect;
import com.tacz.guns.network.message.ClientMessagePlayerMelee;
import com.tacz.guns.network.message.ClientMessagePlayerReloadGun;
import com.tacz.guns.network.message.ClientMessagePlayerShoot;
import com.tacz.guns.network.message.ClientMessagePlayerZoom;
import com.tacz.guns.network.message.ClientMessageRefitGun;
import com.tacz.guns.network.message.ClientMessageSyncBaseTimestamp;
import com.tacz.guns.network.message.ClientMessageUnloadAttachment;
import com.tacz.guns.network.message.ServerMessageCraft;
import com.tacz.guns.network.message.ServerMessageLevelUp;
import com.tacz.guns.network.message.ServerMessageRefreshRefitScreen;
import com.tacz.guns.network.message.ServerMessageSound;
import com.tacz.guns.network.message.ServerMessageSwapItem;
import com.tacz.guns.network.message.ServerMessageSyncBaseTimestamp;
import com.tacz.guns.network.message.ServerMessageSyncRecipes;
import com.tacz.guns.network.message.ServerMessageSyncGunPack;
import com.tacz.guns.network.message.ServerMessageUpdateEntityData;
import com.tacz.guns.network.message.event.ServerMessageGunDraw;
import com.tacz.guns.network.message.event.ServerMessageGunFire;
import com.tacz.guns.network.message.event.ServerMessageGunFireSelect;
import com.tacz.guns.network.message.event.ServerMessageGunHurt;
import com.tacz.guns.network.message.event.ServerMessageGunKill;
import com.tacz.guns.network.message.event.ServerMessageGunMelee;
import com.tacz.guns.network.message.event.ServerMessageGunReload;
import com.tacz.guns.network.message.event.ServerMessageGunShoot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;

public class NetworkHandler {
   private static <T extends CustomPacketPayload> void registerC2SPacket(
      Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, PlayPayloadHandler<T> handler
   ) {
      PayloadTypeRegistry.serverboundPlay().register(type, streamCodec);
      ServerPlayNetworking.registerGlobalReceiver(type, handler);
   }

   private static <T extends CustomPacketPayload> void registerS2CPacket(Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
      PayloadTypeRegistry.clientboundPlay().register(type, streamCodec);
   }

   @Environment(EnvType.CLIENT)
   public static <T extends CustomPacketPayload> void registerClientReceivers() {
      ClientPlayNetworking.registerGlobalReceiver(AdvancedAddEntityPayload.TYPE, AdvancedAddEntityPayload::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageSound.TYPE, ServerMessageSound::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageCraft.TYPE, ServerMessageCraft::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageRefreshRefitScreen.TYPE, ServerMessageRefreshRefitScreen::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageSwapItem.TYPE, ServerMessageSwapItem::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageLevelUp.TYPE, ServerMessageLevelUp::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunHurt.TYPE, ServerMessageGunHurt::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunKill.TYPE, ServerMessageGunKill::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageUpdateEntityData.TYPE, ServerMessageUpdateEntityData::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageSyncGunPack.TYPE, ServerMessageSyncGunPack::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunDraw.TYPE, ServerMessageGunDraw::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunFire.TYPE, ServerMessageGunFire::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunFireSelect.TYPE, ServerMessageGunFireSelect::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunMelee.TYPE, ServerMessageGunMelee::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunReload.TYPE, ServerMessageGunReload::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunShoot.TYPE, ServerMessageGunShoot::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageSyncBaseTimestamp.TYPE, ServerMessageSyncBaseTimestamp::handle);
      ClientPlayNetworking.registerGlobalReceiver(ServerMessageSyncRecipes.TYPE, ServerMessageSyncRecipes::handle);
   }

   public static void registerPackets() {
      registerC2SPackets();
      registerS2CPackets();
   }

   public static void registerC2SPackets() {
      registerC2SPacket(ClientMessagePlayerShoot.TYPE, ClientMessagePlayerShoot.STREAM_CODEC, ClientMessagePlayerShoot::handle);
      registerC2SPacket(ClientMessagePlayerReloadGun.TYPE, ClientMessagePlayerReloadGun.STREAM_CODEC, ClientMessagePlayerReloadGun::handle);
      registerC2SPacket(ClientMessagePlayerCancelReload.TYPE, ClientMessagePlayerCancelReload.STREAM_CODEC, ClientMessagePlayerCancelReload::handle);
      registerC2SPacket(ClientMessagePlayerFireSelect.TYPE, ClientMessagePlayerFireSelect.STREAM_CODEC, ClientMessagePlayerFireSelect::handle);
      registerC2SPacket(ClientMessagePlayerAim.TYPE, ClientMessagePlayerAim.STREAM_CODEC, ClientMessagePlayerAim::handle);
      registerC2SPacket(ClientMessagePlayerCrawl.TYPE, ClientMessagePlayerCrawl.STREAM_CODEC, ClientMessagePlayerCrawl::handle);
      registerC2SPacket(ClientMessagePlayerDrawGun.TYPE, ClientMessagePlayerDrawGun.STREAM_CODEC, ClientMessagePlayerDrawGun::handle);
      registerC2SPacket(ClientMessageCraft.TYPE, ClientMessageCraft.STREAM_CODEC, ClientMessageCraft::handle);
      registerC2SPacket(ClientMessagePlayerZoom.TYPE, ClientMessagePlayerZoom.STREAM_CODEC, ClientMessagePlayerZoom::handle);
      registerC2SPacket(ClientMessageRefitGun.TYPE, ClientMessageRefitGun.STREAM_CODEC, ClientMessageRefitGun::handle);
      registerC2SPacket(ClientMessageUnloadAttachment.TYPE, ClientMessageUnloadAttachment.STREAM_CODEC, ClientMessageUnloadAttachment::handle);
      registerC2SPacket(ClientMessagePlayerBoltGun.TYPE, ClientMessagePlayerBoltGun.STREAM_CODEC, ClientMessagePlayerBoltGun::handle);
      registerC2SPacket(ClientMessagePlayerMelee.TYPE, ClientMessagePlayerMelee.STREAM_CODEC, ClientMessagePlayerMelee::handle);
      registerC2SPacket(ClientMessageSyncBaseTimestamp.TYPE, ClientMessageSyncBaseTimestamp.STREAM_CODEC, ClientMessageSyncBaseTimestamp::handle);
      registerC2SPacket(ClientMessageLaserColor.TYPE, ClientMessageLaserColor.STREAM_CODEC, ClientMessageLaserColor::handle);
   }

   public static void registerS2CPackets() {
      registerS2CPacket(AdvancedAddEntityPayload.TYPE, AdvancedAddEntityPayload.STREAM_CODEC);
      registerS2CPacket(ServerMessageSound.TYPE, ServerMessageSound.STREAM_CODEC);
      registerS2CPacket(ServerMessageCraft.TYPE, ServerMessageCraft.STREAM_CODEC);
      registerS2CPacket(ServerMessageRefreshRefitScreen.TYPE, ServerMessageRefreshRefitScreen.STREAM_CODEC);
      registerS2CPacket(ServerMessageSwapItem.TYPE, ServerMessageSwapItem.STREAM_CODEC);
      registerS2CPacket(ServerMessageLevelUp.TYPE, ServerMessageLevelUp.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunHurt.TYPE, ServerMessageGunHurt.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunKill.TYPE, ServerMessageGunKill.STREAM_CODEC);
      registerS2CPacket(ServerMessageUpdateEntityData.TYPE, ServerMessageUpdateEntityData.STREAM_CODEC);
      registerS2CPacket(ServerMessageSyncGunPack.TYPE, ServerMessageSyncGunPack.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunDraw.TYPE, ServerMessageGunDraw.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunFire.TYPE, ServerMessageGunFire.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunFireSelect.TYPE, ServerMessageGunFireSelect.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunMelee.TYPE, ServerMessageGunMelee.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunReload.TYPE, ServerMessageGunReload.STREAM_CODEC);
      registerS2CPacket(ServerMessageGunShoot.TYPE, ServerMessageGunShoot.STREAM_CODEC);
      registerS2CPacket(ServerMessageSyncBaseTimestamp.TYPE, ServerMessageSyncBaseTimestamp.STREAM_CODEC);
      registerS2CPacket(ServerMessageSyncRecipes.TYPE, ServerMessageSyncRecipes.STREAM_CODEC);
   }

   public static void sendToClientPlayer(CustomPacketPayload message, ServerPlayer player) {
      ServerPlayNetworking.send(player, message);
   }

   public static void sendToTrackingEntityAndSelf(Entity centerEntity, CustomPacketPayload message) {
      if (centerEntity instanceof ServerPlayer player) {
         sendToClientPlayer(message, player);
      }

      sendToTrackingEntity(message, centerEntity);
   }

   public static void sendToAllPlayers(CustomPacketPayload message, MinecraftServer server) {
      for (ServerPlayer player : PlayerLookup.all(server)) {
         ServerPlayNetworking.send(player, message);
      }
   }

   public static void sendToTrackingEntity(CustomPacketPayload message, Entity centerEntity) {
      for (ServerPlayer player : PlayerLookup.tracking(centerEntity)) {
         ServerPlayNetworking.send(player, message);
      }
   }

   public static void sendToDimension(CustomPacketPayload message, Entity centerEntity) {
      if (centerEntity.level() instanceof ServerLevel serverLevel) {
         for (ServerPlayer player : serverLevel.players()) {
            ServerPlayNetworking.send(player, message);
         }
      }
   }

   public static void sendRecipes(net.minecraft.server.MinecraftServer server, java.util.Collection<ServerPlayer> players) {
      java.util.Map<net.minecraft.resources.Identifier, com.tacz.guns.crafting.GunSmithTableRecipe> map = new java.util.LinkedHashMap<>();
      for (net.minecraft.world.item.crafting.RecipeHolder<com.tacz.guns.crafting.GunSmithTableRecipe> holder : com.tacz.guns.init.ModRecipe.allRecipes(server.getRecipeManager())) {
         map.put(holder.id().identifier(), holder.value());
      }

      ServerMessageSyncRecipes message = new ServerMessageSyncRecipes(map);
      for (ServerPlayer player : players) {
         sendToClientPlayer(message, player);
      }
   }
}
