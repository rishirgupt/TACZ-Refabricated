package com.tacz.guns.network.message;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.config.sync.SyncConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ClientMessagePlayerCrawl(boolean isCrawl) implements CustomPacketPayload {
   public static final Type<ClientMessagePlayerCrawl> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_player_crawl"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessagePlayerCrawl> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL, message -> message.isCrawl, ClientMessagePlayerCrawl::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessagePlayerCrawl message, Context context) {
      context.server().execute(() -> {
         ServerPlayer entity = context.player();
         if ((Boolean)SyncConfig.ENABLE_CRAWL.get()) {
            IGunOperator.fromLivingEntity(entity).crawl(message.isCrawl);
         }
      });
   }
}
