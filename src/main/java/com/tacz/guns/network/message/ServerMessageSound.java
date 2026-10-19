package com.tacz.guns.network.message;

import com.tacz.guns.client.sound.SoundPlayManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ServerMessageSound(int entityId, Identifier gunId, Identifier gunDisplayId, ServerMessageSound.MessageSound messageSound) implements CustomPacketPayload {
   public static final Type<ServerMessageSound> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_sound"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageSound> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      ServerMessageSound::entityId,
      Identifier.STREAM_CODEC,
      ServerMessageSound::gunId,
      Identifier.STREAM_CODEC,
      ServerMessageSound::gunDisplayId,
      ServerMessageSound.MessageSound.STREAM_CODEC,
      ServerMessageSound::messageSound,
      ServerMessageSound::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageSound message, Context context) {
      context.client().execute(() -> SoundPlayManager.playMessageSound(message));
   }

   public record MessageSound(String soundName, float volume, float pitch, int distance) {
      public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageSound.MessageSound> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8,
         ServerMessageSound.MessageSound::soundName,
         ByteBufCodecs.FLOAT,
         ServerMessageSound.MessageSound::volume,
         ByteBufCodecs.FLOAT,
         ServerMessageSound.MessageSound::pitch,
         ByteBufCodecs.INT,
         ServerMessageSound.MessageSound::distance,
         ServerMessageSound.MessageSound::new
      );
   }
}
