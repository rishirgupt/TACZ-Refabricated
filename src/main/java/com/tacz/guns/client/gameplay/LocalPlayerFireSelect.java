package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.event.common.GunFireSelectEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerFireSelect;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerFireSelect {
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;

   public LocalPlayerFireSelect(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public void fireSelect() {
      if (!this.data.clientStateLock) {
         ItemStack mainHandItem = this.player.getMainHandItem();
         if (mainHandItem.getItem() instanceof IGun iGun) {
            GunFireSelectEvent var4 = new GunFireSelectEvent(this.player, this.player.getMainHandItem(), LogicalSide.CLIENT);
            ((GunFireSelectEvent.Callback)GunFireSelectEvent.CALLBACK.invoker()).post(var4);
            if (!var4.isCanceled()) {
               TimelessAPI.getGunDisplay(mainHandItem).ifPresent(gunIndex -> {
                  SoundPlayManager.playFireSelectSound(this.player, gunIndex);
                  ClientPlayNetworking.send(ClientMessagePlayerFireSelect.INSTANCE);
                  if (iGun instanceof AbstractGunItem logicGun) {
                     logicGun.fireSelect(null, mainHandItem);
                  }

                  AttachmentPropertyManager.postChangeEvent(this.player, mainHandItem);
                  AnimationStateMachine<?> animationStateMachine = gunIndex.getAnimationStateMachine();
                  if (animationStateMachine != null) {
                     animationStateMachine.trigger("fire_select");
                  }
               });
            }
         }
      }
   }
}
