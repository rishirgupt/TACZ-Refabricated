package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.mixin.accessor.BlockableEventLoopAccessor;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunDrawEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerDrawGun;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerDraw {
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;
   public boolean readyToDraw = false;

   public LocalPlayerDraw(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public void draw(ItemStack lastItem) {
      this.resetData();
      ItemStack currentItem = this.player.getMainHandItem();
      long drawTime = System.currentTimeMillis() - this.data.clientDrawTimestamp;
      IGun currentGun = IGun.getIGunOrNull(currentItem);
      IGun lastGun = IGun.getIGunOrNull(lastItem);
      if (drawTime >= 0L) {
         drawTime = this.getDrawTime(lastItem, lastGun, drawTime);
      }

      long putAwayTime = Math.abs(drawTime);
      if (Minecraft.getInstance().gameMode != null) {
         Minecraft.getInstance().gameMode.ensureHasSentCarriedItem();
      }

      ClientPlayNetworking.send(ClientMessagePlayerDrawGun.INSTANCE);
      ((GunDrawEvent.Callback)GunDrawEvent.CALLBACK.invoker()).post(new GunDrawEvent(this.player, lastItem, currentItem, LogicalSide.CLIENT));
      if (drawTime >= 0L) {
         this.doPutAway(lastItem, putAwayTime);
      }

      if (currentGun != null) {
         this.doDraw(currentItem, putAwayTime);
         AttachmentPropertyManager.postChangeEvent(this.player, currentItem);
      }
   }

   private void doDraw(ItemStack currentItem, long putAwayTime) {
      TimelessAPI.getGunDisplay(currentItem)
         .ifPresent(
            display -> {
               if (this.data.drawFuture != null) {
                  this.data.drawFuture.cancel(false);
               }

               this.data.drawFuture = LocalPlayerDataHolder.SCHEDULED_EXECUTOR_SERVICE
                  .schedule(() -> ((BlockableEventLoopAccessor)Minecraft.getInstance()).tacz$submitAsync(() -> {
                     SoundPlayManager.stopPlayGunSound();
                     SoundPlayManager.playDrawSound(this.player, display);
                  }), putAwayTime, TimeUnit.MILLISECONDS);
            }
         );
   }

   private void doPutAway(ItemStack lastItem, long putAwayTime) {
      if (com.tacz.guns.client.render26.GeoItemRegistry.get(lastItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
         renderer.tryExit(lastItem, putAwayTime);
      }

      TimelessAPI.getGunDisplay(lastItem).ifPresent(display -> ((BlockableEventLoopAccessor)Minecraft.getInstance()).tacz$submitAsync(() -> {
         SoundPlayManager.stopPlayGunSound();
         SoundPlayManager.playPutAwaySound(this.player, display);
      }));
   }

   private long getDrawTime(ItemStack lastItem, IGun lastGun, long drawTime) {
      if (com.tacz.guns.client.render26.GeoItemRegistry.get(lastItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
         long putAwayTime = renderer.getPutAwayTime(lastItem);
         if (drawTime > putAwayTime) {
            drawTime = putAwayTime;
         }

         this.data.clientDrawTimestamp = System.currentTimeMillis() + drawTime;
      } else {
         drawTime = 0L;
         this.data.clientDrawTimestamp = System.currentTimeMillis();
      }

      return drawTime;
   }

   private void resetData() {
      this.data.lockState(operator -> operator.getSynDrawCoolDown() > 0L);
      this.data.isShootRecorded = true;
      this.data.clientShootTimestamp = -1L;
      this.data.chargeProgress = 0.0F;
      this.data.clientIsAiming = false;
      this.data.clientAimingProgress = 0.0F;
      LocalPlayerDataHolder.oldAimingProgress = 0.0F;
      this.data.isBolting = false;
      if (this.data.clientDrawTimestamp == -1L) {
         this.data.clientDrawTimestamp = System.currentTimeMillis();
      }
   }
}
