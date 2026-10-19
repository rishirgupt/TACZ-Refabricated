package com.tacz.guns.network.message.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
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

public record ServerMessageGunHurt(
   int bulletId, int hurtEntityId, int attackerId, Identifier gunId, Identifier gunDisplayId, float amount, boolean isHeadShot, float headshotMultiplier
) implements CustomPacketPayload {
   public static final Type<ServerMessageGunHurt> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_gun_hurt"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageGunHurt> STREAM_CODEC = StreamCodec.of(
      ServerMessageGunHurt::encode, ServerMessageGunHurt::decode
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void encode(RegistryFriendlyByteBuf buf, ServerMessageGunHurt message) {
      buf.writeInt(message.bulletId);
      buf.writeInt(message.hurtEntityId);
      buf.writeInt(message.attackerId);
      buf.writeIdentifier(message.gunId);
      buf.writeIdentifier(message.gunDisplayId);
      buf.writeFloat(message.amount);
      buf.writeBoolean(message.isHeadShot);
      buf.writeFloat(message.headshotMultiplier);
   }

   public static ServerMessageGunHurt decode(RegistryFriendlyByteBuf buf) {
      int bulletId = buf.readInt();
      int hurtEntityId = buf.readInt();
      int attackerId = buf.readInt();
      Identifier gunId = buf.readIdentifier();
      Identifier gunDisplayId = buf.readIdentifier();
      float amount = buf.readFloat();
      boolean isHeadShot = buf.readBoolean();
      float headshotMultiplier = buf.readFloat();
      return new ServerMessageGunHurt(bulletId, hurtEntityId, attackerId, gunId, gunDisplayId, amount, isHeadShot, headshotMultiplier);
   }

   public static void handle(ServerMessageGunHurt message, Context context) {
      context.client().execute(() -> onHurt(message));
   }

   @Environment(EnvType.CLIENT)
   private static void onHurt(ServerMessageGunHurt message) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         Entity bullet = level.getEntity(message.bulletId);
         Entity hurtEntity = level.getEntity(message.hurtEntityId);
         LivingEntity attacker = level.getEntity(message.attackerId) instanceof LivingEntity livingEntity ? livingEntity : null;
         ((EntityHurtByGunEvent.PostCallBack)EntityHurtByGunEvent.POST.invoker())
            .post(
               new EntityHurtByGunEvent.Post(
                  bullet,
                  hurtEntity,
                  attacker,
                  message.gunId,
                  message.gunDisplayId,
                  message.amount,
                  null,
                  message.isHeadShot,
                  message.headshotMultiplier,
                  LogicalSide.CLIENT
               )
            );
      }
   }
}
