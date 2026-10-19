package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerInspect {
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;

   public LocalPlayerInspect(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public void inspect() {
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof IGun iGun) {
         if (!this.data.clientStateLock) {
            GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).map(ClientGunIndex::getGunData).orElse(null);
            if (gunData != null) {
               TimelessAPI.getGunDisplay(mainHandItem).ifPresent(gunIndex -> {
                  Bolt boltType = gunData.getBolt();
                  boolean noAmmo;
                  if (boltType == Bolt.OPEN_BOLT) {
                     noAmmo = iGun.getCurrentAmmoCount(mainHandItem) <= 0;
                  } else {
                     noAmmo = !iGun.hasBulletInBarrel(mainHandItem);
                  }

                  SoundPlayManager.stopPlayGunSound();
                  SoundPlayManager.playInspectSound(this.player, gunIndex, noAmmo);
                  LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = gunIndex.getAnimationStateMachine();
                  if (animationStateMachine != null) {
                     animationStateMachine.trigger("inspect");
                  }
               });
            }
         }
      } else {
         if (com.tacz.guns.client.render26.GeoItemRegistry.get(mainHandItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
            renderer.triggerAnimation(mainHandItem, "inspect");
         }
      }
   }
}
