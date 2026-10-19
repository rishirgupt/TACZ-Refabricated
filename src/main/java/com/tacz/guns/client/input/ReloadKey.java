package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import cn.sh1rocu.tacz.api.event.PlayerTickEvent;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.client.KeyConfig;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class ReloadKey {
   public static final KeyMapping RELOAD_KEY = new KeyMapping("key.tacz.reload.desc", Type.KEYSYM, 82, KeyUtil.CATEGORY);

   public static void onReloadPress(InputEvent.Key event) {
      if (InputExtraCheck.isInGame() && event.getAction() == 1 && KeyUtil.matches(RELOAD_KEY, event.getKey(), event.getScanCode())) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return;
         }

         if (player.getMainHandItem().getItem() instanceof IGun iGun) {
            if (iGun.useInventoryAmmo(player.getMainHandItem())) {
               return;
            }

            IClientPlayerGunOperator.fromLocalPlayer(player).reload();
         }
      }
   }

   public static boolean onReloadControllerPress(boolean isPress) {
      if (InputExtraCheck.isInGame() && isPress) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator()) {
            return false;
         }

         if (IGun.mainHandHoldGun(player)) {
            IClientPlayerGunOperator.fromLocalPlayer(player).reload();
            return true;
         }
      }

      return false;
   }

   @Environment(EnvType.CLIENT)
   public static void autoReload(PlayerTickEvent.Pre event) {
      if (event.getEntity().level().isClientSide()) {
         if ((Boolean)KeyConfig.AUTO_RELOAD.get()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isSpectator() && player.tickCount % 5 == 0) {
               ItemStack currentGunItem = player.getMainHandItem();
               if (player.getMainHandItem().getItem() instanceof IGun iGun) {
                  if (iGun.useInventoryAmmo(player.getMainHandItem())) {
                     return;
                  }

                  boolean flag = TimelessAPI.getCommonGunIndex(iGun.getGunId(currentGunItem))
                     .map(gunIndex -> gunIndex.getGunData().getBolt() != Bolt.OPEN_BOLT)
                     .orElse(false);
                  int ammoCount = iGun.getCurrentAmmoCount(currentGunItem) + (iGun.hasBulletInBarrel(currentGunItem) && flag ? 1 : 0);
                  if (ammoCount > 0) {
                     return;
                  }

                  IClientPlayerGunOperator.fromLocalPlayer(player).reload();
               }
            }
         }
      }
   }
}
