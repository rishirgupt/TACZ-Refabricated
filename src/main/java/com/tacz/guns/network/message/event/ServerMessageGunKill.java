package com.tacz.guns.network.message.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ServerMessageGunKill(
   int bulletId, int killEntityId, int attackerId, Identifier gunId, Identifier gunDisplayId, float baseDamage, boolean isHeadShot, float headshotMultiplier
) implements CustomPacketPayload {
   public static final Type<ServerMessageGunKill> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_gun_kill"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageGunKill> STREAM_CODEC = StreamCodec.of(
      ServerMessageGunKill::encode, ServerMessageGunKill::decode
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void encode(RegistryFriendlyByteBuf buf, ServerMessageGunKill message) {
      buf.writeInt(message.bulletId);
      buf.writeInt(message.killEntityId);
      buf.writeInt(message.attackerId);
      buf.writeIdentifier(message.gunId);
      buf.writeIdentifier(message.gunDisplayId);
      buf.writeFloat(message.baseDamage);
      buf.writeBoolean(message.isHeadShot);
      buf.writeFloat(message.headshotMultiplier);
   }

   public static ServerMessageGunKill decode(RegistryFriendlyByteBuf buf) {
      int bulletId = buf.readInt();
      int killEntityId = buf.readInt();
      int attackerId = buf.readInt();
      Identifier gunId = buf.readIdentifier();
      Identifier gunDisplayId = buf.readIdentifier();
      float baseDamage = buf.readFloat();
      boolean isHeadShot = buf.readBoolean();
      float headshotMultiplier = buf.readFloat();
      return new ServerMessageGunKill(bulletId, killEntityId, attackerId, gunId, gunDisplayId, baseDamage, isHeadShot, headshotMultiplier);
   }

   public static void handle(ServerMessageGunKill message, Context context) {
      context.client().execute(() -> onKill(message));
   }

   @Environment(EnvType.CLIENT)
   private static void onKill(ServerMessageGunKill message) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         Entity bullet = level.getEntity(message.bulletId);
         LivingEntity killedEntity = level.getEntity(message.killEntityId) instanceof LivingEntity livingEntity ? livingEntity : null;
         LivingEntity attacker = level.getEntity(message.attackerId) instanceof LivingEntity livingEntity ? livingEntity : null;
         ((EntityKillByGunEvent.Callback)EntityKillByGunEvent.CALLBACK.invoker())
            .post(
               new EntityKillByGunEvent(
                  bullet,
                  killedEntity,
                  attacker,
                  message.gunId,
                  message.gunDisplayId,
                  message.baseDamage,
                  null,
                  message.isHeadShot,
                  message.headshotMultiplier,
                  LogicalSide.CLIENT
               )
            );
      }
   }
}
