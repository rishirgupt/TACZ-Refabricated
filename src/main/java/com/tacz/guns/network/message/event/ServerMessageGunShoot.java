package com.tacz.guns.network.message.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.event.common.GunShootEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public class ServerMessageGunShoot implements CustomPacketPayload {
   public static final Type<ServerMessageGunShoot> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_gun_shoot"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageGunShoot> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, message -> message.shooterId, ItemStack.STREAM_CODEC, message -> message.gunItemStack, ServerMessageGunShoot::new
   );
   private final int shooterId;
   private final ItemStack gunItemStack;

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public ServerMessageGunShoot(int shooterId, ItemStack gunItemStack) {
      this.shooterId = shooterId;
      this.gunItemStack = gunItemStack;
   }

   public static void handle(ServerMessageGunShoot message, Context context) {
      context.client().execute(() -> doClientEvent(message));
   }

   @Environment(EnvType.CLIENT)
   private static void doClientEvent(ServerMessageGunShoot message) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         if (level.getEntity(message.shooterId) instanceof LivingEntity shooter) {
            GunShootEvent gunShootEvent = new GunShootEvent(shooter, message.gunItemStack, LogicalSide.CLIENT);
            ((GunShootEvent.Callback)GunShootEvent.CALLBACK.invoker()).post(gunShootEvent);
         }
      }
   }
}
