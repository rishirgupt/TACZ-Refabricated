package com.tacz.guns.network.message;

import com.tacz.guns.client.resource.ClientIndexManager;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.network.CommonNetworkCache;
import com.tacz.guns.resource.network.DataType;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public class ServerMessageSyncGunPack implements CustomPacketPayload {
   public static final Type<ServerMessageSyncGunPack> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_sync_gun_pack"));
   public static final StreamCodec<FriendlyByteBuf, ServerMessageSyncGunPack> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.map(
         HashMap::new, ByteBufCodecs.fromCodec(DataType.CODEC), ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.STRING_UTF8)
      ),
      ServerMessageSyncGunPack::getCache,
      ServerMessageSyncGunPack::new
   );
   private final Map<DataType, Map<Identifier, String>> cache;

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public ServerMessageSyncGunPack(Map<DataType, Map<Identifier, String>> cache) {
      this.cache = cache;
   }

   public static void handle(ServerMessageSyncGunPack message, Context context) {
      context.client().execute(() -> {
         ClientPacketListener connection = context.client().getConnection();
         boolean remoteConnection = connection != null && !connection.getConnection().isMemoryConnection();
         doSync(message, remoteConnection);
      });
   }

   public Map<DataType, Map<Identifier, String>> getCache() {
      return this.cache;
   }

   @Environment(EnvType.CLIENT)
   private static void doSync(ServerMessageSyncGunPack message, boolean remoteConnection) {
      if (remoteConnection) {
         CommonAssetsManager.clearInstance();
      }

      CommonNetworkCache.INSTANCE.fromNetwork(message.cache);
      ClientIndexManager.reload();
   }
}
