package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.client.KeyConfig;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants.Type;

@Environment(EnvType.CLIENT)
public class CrawlKey {
   public static final KeyMapping CRAWL_KEY = new KeyMapping("key.tacz.crawl.desc", Type.KEYSYM, 67, KeyUtil.CATEGORY);

   public static void onCrawlPress(InputEvent.Key event) {
      if (InputExtraCheck.isInGame() && KeyUtil.matches(CRAWL_KEY, event.getKey(), event.getScanCode())) {
         if (!(Boolean)SyncConfig.ENABLE_CRAWL.get()) {
            return;
         }

         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null || player.isSpectator() || player.isPassenger()) {
            return;
         }

         if (!(player instanceof IClientPlayerGunOperator operator)) {
            return;
         }

         if (player.getMainHandItem().getItem() instanceof IGun iGun) {
            if (!iGun.isCanCrawl(player.getMainHandItem())) {
               IClientPlayerGunOperator.fromLocalPlayer(player).crawl(false);
               return;
            }

            boolean action = true;
            if (!(Boolean)KeyConfig.HOLD_TO_CRAWL.get()) {
               action = !operator.isCrawl();
            }

            if (event.getAction() == 1) {
               IClientPlayerGunOperator.fromLocalPlayer(player).crawl(action);
            }

            if ((Boolean)KeyConfig.HOLD_TO_CRAWL.get() && event.getAction() == 0) {
               IClientPlayerGunOperator.fromLocalPlayer(player).crawl(false);
            }
         }
      }
   }

   public static boolean onCrawlControllerPress(boolean isPress) {
      if (!InputExtraCheck.isInGame()) {
         return false;
      }

      if (!(Boolean)SyncConfig.ENABLE_CRAWL.get()) {
         return false;
      }

      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null || player.isSpectator() || player.isPassenger()) {
         return false;
      }

      if (player instanceof IClientPlayerGunOperator operator) {
         if (!IGun.mainHandHoldGun(player)) {
            return false;
         }

         boolean action = true;
         if (!(Boolean)KeyConfig.HOLD_TO_CRAWL.get()) {
            action = !operator.isCrawl();
         }

         if (isPress) {
            IClientPlayerGunOperator.fromLocalPlayer(player).crawl(action);
            return true;
         } else if ((Boolean)KeyConfig.HOLD_TO_CRAWL.get()) {
            IClientPlayerGunOperator.fromLocalPlayer(player).crawl(false);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
