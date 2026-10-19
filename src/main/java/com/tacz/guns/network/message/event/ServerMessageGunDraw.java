package com.tacz.guns.network.message.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.event.common.GunDrawEvent;
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

public record ServerMessageGunDraw(int entityId, ItemStack previousGunItem, ItemStack currentGunItem) implements CustomPacketPayload {
   public static final Type<ServerMessageGunDraw> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "server_gun_draw"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageGunDraw> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      message -> message.entityId,
      ItemStack.OPTIONAL_STREAM_CODEC,
      message -> message.previousGunItem,
      ItemStack.OPTIONAL_STREAM_CODEC,
      message -> message.currentGunItem,
      ServerMessageGunDraw::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ServerMessageGunDraw message, Context context) {
      context.client().execute(() -> doClientEvent(message));
   }

   @Environment(EnvType.CLIENT)
   private static void doClientEvent(ServerMessageGunDraw message) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         if (level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
            GunDrawEvent gunDrawEvent = new GunDrawEvent(livingEntity, message.previousGunItem, message.currentGunItem, LogicalSide.CLIENT);
            ((GunDrawEvent.Callback)GunDrawEvent.CALLBACK.invoker()).post(gunDrawEvent);
         }
      }
   }
}
