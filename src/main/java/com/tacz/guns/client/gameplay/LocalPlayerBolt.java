package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerBoltGun;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerBolt {
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;

   public LocalPlayerBolt(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public void bolt() {
      if (!this.data.clientStateLock) {
         if (!this.data.isBolting) {
            ItemStack mainHandItem = this.player.getMainHandItem();
            if (mainHandItem.getItem() instanceof IGun iGun) {
               GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).map(ClientGunIndex::getGunData).orElse(null);
               if (gunData != null) {
                  TimelessAPI.getGunDisplay(mainHandItem).ifPresent(display -> {
                     IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.player);
                     Bolt boltType = gunData.getBolt();
                     boolean useInventoryAmmo = iGun.useInventoryAmmo(mainHandItem);
                     boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainHandItem) && boltType != Bolt.OPEN_BOLT;
                     boolean hasInventoryAmmo = iGun.hasInventoryAmmo(this.player, mainHandItem, gunOperator.needCheckAmmo());
                     boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo || !useInventoryAmmo && iGun.getCurrentAmmoCount(mainHandItem) < 1;
                     if (boltType == Bolt.MANUAL_ACTION) {
                        if (!hasAmmoInBarrel) {
                           if (!noAmmo) {
                              this.data.lockState(IGunOperator::getSynIsBolting);
                              this.data.isBolting = true;
                              ClientPlayNetworking.send(ClientMessagePlayerBoltGun.INSTANCE);
                              AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
                              if (animationStateMachine != null) {
                                 SoundPlayManager.playBoltSound(this.player, display);
                                 animationStateMachine.trigger("blot");
                              }
                           }
                        }
                     }
                  });
               }
            }
         }
      }
   }

   public void tickAutoBolt() {
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof IGun iGun) {
         this.bolt();
         if (this.data.isBolting && iGun.hasBulletInBarrel(mainHandItem)) {
            this.data.isBolting = false;
         }
      } else {
         this.data.isBolting = false;
      }
   }
}
