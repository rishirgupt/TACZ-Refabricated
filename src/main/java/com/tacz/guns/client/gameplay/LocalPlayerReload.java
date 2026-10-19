package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.event.common.GunReloadEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerCancelReload;
import com.tacz.guns.network.message.ClientMessagePlayerReloadGun;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerReload {
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;

   public LocalPlayerReload(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public void cancelReload() {
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof AbstractGunItem) {
         TimelessAPI.getGunDisplay(mainHandItem).ifPresent(display -> {
            IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.player);
            ReloadState reloadState = gunOperator.getSynReloadState();
            if (reloadState.getStateType().isReloading()) {
               ClientPlayNetworking.send(ClientMessagePlayerCancelReload.INSTANCE);
               this.cancelReload(display);
            }
         });
      }
   }

   public void reload() {
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof AbstractGunItem gunItem) {
         Identifier gunId = gunItem.getGunId(mainHandItem);
         GunData gunData = TimelessAPI.getClientGunIndex(gunId).map(ClientGunIndex::getGunData).orElse(null);
         if (gunData != null) {
            TimelessAPI.getGunDisplay(mainHandItem).ifPresent(display -> {
               if (!gunItem.useInventoryAmmo(mainHandItem)) {
                  if (!this.data.clientStateLock) {
                     if (System.currentTimeMillis() - this.data.clientShootTimestamp >= 100L) {
                        boolean canReload = gunItem.canReload(this.player, mainHandItem);
                        if (!IGunOperator.fromLivingEntity(this.player).needCheckAmmo() || canReload) {
                           this.data.lockState(operator -> operator.getSynReloadState().getStateType().isReloading());
                           this.data.chargeProgress = 0.0F;
                           GunReloadEvent gunReloadEvent = new GunReloadEvent(this.player, this.player.getMainHandItem(), LogicalSide.CLIENT);
                           ((GunReloadEvent.Callback)GunReloadEvent.CALLBACK.invoker()).post(gunReloadEvent);
                           if (!gunReloadEvent.isCanceled()) {
                              ClientPlayNetworking.send(ClientMessagePlayerReloadGun.INSTANCE);
                              this.doReload(gunItem, display, gunData, mainHandItem);
                           }
                        }
                     }
                  }
               }
            });
         }
      }
   }

   private void doReload(IGun iGun, GunDisplayInstance display, GunData gunData, ItemStack mainHandItem) {
      LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = display.getAnimationStateMachine();
      if (animationStateMachine != null) {
         Bolt boltType = gunData.getBolt();
         boolean noAmmo;
         if (boltType == Bolt.OPEN_BOLT) {
            noAmmo = iGun.getCurrentAmmoCount(mainHandItem) <= 0;
         } else {
            noAmmo = !iGun.hasBulletInBarrel(mainHandItem);
         }

         SoundPlayManager.stopPlayGunSound();
         SoundPlayManager.playReloadSound(this.player, display, noAmmo);
         animationStateMachine.trigger("reload");
      }
   }

   private void cancelReload(GunDisplayInstance display) {
      LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = display.getAnimationStateMachine();
      if (animationStateMachine != null) {
         animationStateMachine.trigger("cancel_reload");
      }
   }
}
