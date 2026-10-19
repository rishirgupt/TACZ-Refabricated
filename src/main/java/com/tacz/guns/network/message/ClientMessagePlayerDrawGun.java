package com.tacz.guns.network.message;

import com.tacz.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public class ClientMessagePlayerDrawGun implements CustomPacketPayload {
   public static final ClientMessagePlayerDrawGun INSTANCE = new ClientMessagePlayerDrawGun();
   public static final Type<ClientMessagePlayerDrawGun> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_player_draw_gun"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessagePlayerDrawGun> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessagePlayerDrawGun message, Context context) {
      context.server().execute(() -> {
         ServerPlayer entity = context.player();
         Inventory inventory = entity.getInventory();
         int selected = inventory.getSelectedSlot();
         IGunOperator.fromLivingEntity(entity).draw(() -> inventory.getItem(selected));
      });
   }
}
