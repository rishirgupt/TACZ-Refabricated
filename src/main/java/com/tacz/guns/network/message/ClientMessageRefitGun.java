package com.tacz.guns.network.message;

import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.NotNull;

public record ClientMessageRefitGun(int attachmentSlotIndex, int gunSlotIndex, AttachmentType attachmentType) implements CustomPacketPayload {
   public static final Type<ClientMessageRefitGun> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "client_refit_gun"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientMessageRefitGun> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      message -> message.attachmentSlotIndex,
      ByteBufCodecs.INT,
      message -> message.gunSlotIndex,
      ByteBufCodecs.fromCodec(AttachmentType.CODEC),
      message -> message.attachmentType,
      ClientMessageRefitGun::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ClientMessageRefitGun message, Context context) {
      context.server().execute(() -> {
         ServerPlayer player = context.player();
         Inventory inventory = player.getInventory();
         ItemStack attachmentItem = inventory.getItem(message.attachmentSlotIndex);
         ItemStack gunItem = inventory.getItem(message.gunSlotIndex);
         IGun iGun = IGun.getIGunOrNull(gunItem);
         if (iGun != null) {
            if (iGun.hasAttachmentLock(gunItem)) {
               return;
            }

            if (iGun.allowAttachment(gunItem, attachmentItem)) {
               IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
               if (iAttachment == null) {
                  return;
               }

               AttachmentType realType = iAttachment.getType(attachmentItem);
               ItemStack oldAttachmentItem = iGun.getAttachment(player.registryAccess(), gunItem, realType);
               iGun.installAttachment(player.registryAccess(), gunItem, attachmentItem);
               AttachmentPropertyManager.postChangeEvent(player, gunItem);
               inventory.setItem(message.attachmentSlotIndex, oldAttachmentItem);
               if (realType == AttachmentType.EXTENDED_MAG) {
                  iGun.dropAllAmmo(player, gunItem);
               }

               player.inventoryMenu.broadcastChanges();
               NetworkHandler.sendToClientPlayer(ServerMessageRefreshRefitScreen.INSTANCE, player);
            }
         }
      });
   }
}
