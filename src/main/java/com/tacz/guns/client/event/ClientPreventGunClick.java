package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.input.InteractKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class ClientPreventGunClick {
   public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         if (!InteractKey.INTERACT_KEY.isDown()) {
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemInHand.getItem() instanceof IGun) {
               if (Minecraft.getInstance().hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof ItemFrame) {
                  return;
               }

               event.setSwingHand(false);
               event.setCanceled(true);
            }
         }
      }
   }
}
