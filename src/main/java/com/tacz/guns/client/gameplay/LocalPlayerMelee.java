package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.event.common.GunMeleeEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerMelee;
import com.tacz.guns.resource.pojo.data.attachment.MeleeData;
import com.tacz.guns.resource.pojo.data.gun.GunDefaultMeleeData;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerMelee {
   public static final String MELEE_STOCK_ANIMATION = "melee_stock";
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;

   public LocalPlayerMelee(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public void melee() {
      if (!this.data.clientStateLock) {
         ItemStack mainHandItem = this.player.getMainHandItem();
         if (mainHandItem.getItem() instanceof IGun iGun) {
            GunDisplayInstance display = TimelessAPI.getGunDisplay(mainHandItem).orElse(null);
            if (display != null) {
               Identifier gunId = iGun.getGunId(mainHandItem);
               Identifier muzzleId = iGun.getAttachmentId(mainHandItem, AttachmentType.MUZZLE);
               MeleeData muzzleMeleeData = this.getMeleeData(muzzleId);
               if (muzzleMeleeData != null) {
                  this.doMuzzleMelee(display);
               } else {
                  Identifier stockId = iGun.getAttachmentId(mainHandItem, AttachmentType.STOCK);
                  MeleeData stockMeleeData = this.getMeleeData(stockId);
                  if (stockMeleeData != null) {
                     this.doStockMelee(display);
                  } else {
                     TimelessAPI.getClientGunIndex(gunId).ifPresent(index -> {
                        GunDefaultMeleeData defaultMeleeData = index.getGunData().getMeleeData().getDefaultMeleeData();
                        if (defaultMeleeData != null) {
                           String animationType = defaultMeleeData.getAnimationType();
                           if ("melee_stock".equals(animationType)) {
                              this.doStockMelee(display);
                           } else {
                              this.doPushMelee(display);
                           }
                        }
                     });
                  }
               }
            }
         }
      }
   }

   private boolean prepareMelee() {
      this.data.lockState(operator -> operator.getSynMeleeCoolDown() > 0L);
      GunMeleeEvent gunMeleeEvent = new GunMeleeEvent(this.player, this.player.getMainHandItem(), LogicalSide.CLIENT);
      ((GunMeleeEvent.Callback)GunMeleeEvent.CALLBACK.invoker()).post(gunMeleeEvent);
      return !gunMeleeEvent.isCanceled();
   }

   private void doMuzzleMelee(GunDisplayInstance display) {
      if (this.prepareMelee()) {
         SoundPlayManager.playMeleeBayonetSound(this.player, display);
         ClientPlayNetworking.send(ClientMessagePlayerMelee.INSTANCE);
         AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
         if (animationStateMachine != null) {
            animationStateMachine.trigger("bayonet_muzzle");
         }
      }
   }

   private void doStockMelee(GunDisplayInstance display) {
      if (this.prepareMelee()) {
         SoundPlayManager.playMeleeStockSound(this.player, display);
         ClientPlayNetworking.send(ClientMessagePlayerMelee.INSTANCE);
         AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
         if (animationStateMachine != null) {
            animationStateMachine.trigger("bayonet_stock");
         }
      }
   }

   private void doPushMelee(GunDisplayInstance display) {
      if (this.prepareMelee()) {
         SoundPlayManager.playMeleePushSound(this.player, display);
         ClientPlayNetworking.send(ClientMessagePlayerMelee.INSTANCE);
         AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
         if (animationStateMachine != null) {
            animationStateMachine.trigger("bayonet_push");
         }
      }
   }

   @Nullable
   private MeleeData getMeleeData(Identifier attachmentId) {
      return DefaultAssets.isEmptyAttachmentId(attachmentId)
         ? null
         : TimelessAPI.getClientAttachmentIndex(attachmentId).map(index -> index.getData().getMeleeData()).orElse(null);
   }
}
