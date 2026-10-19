package com.tacz.guns.client.event;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent.Phase;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class TickAnimationEvent {
   public static void tickAnimation(Minecraft client) {
      LocalPlayer player = client.player;
      if (player != null) {
         ItemStack mainHandItem = player.getMainHandItem();
         TimelessAPI.getGunDisplay(mainHandItem).ifPresent(gunIndex -> {
            LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine != null) {
               if (player.input == null) {
                  animationStateMachine.trigger("idle");
               } else {
                  if (!player.isMovingSlowly() && player.isSprinting()) {
                     animationStateMachine.trigger("run");
                  } else if (!player.isMovingSlowly() && player.input.getMoveVector().length() > 0.01) {
                     animationStateMachine.trigger("walk");
                  } else {
                     animationStateMachine.trigger("idle");
                  }
               }
            }
         });
      }
   }

   public static void tickAnimation(RenderTickEvent event) {
      if (event.phase != Phase.END) {
         if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
               ItemStack mainHandItem = player.getMainHandItem();
               if (com.tacz.guns.client.render26.GeoItemRegistry.get(mainHandItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
                  if (renderer.needReInit(mainHandItem)) {
                     renderer.tryInit(mainHandItem, player, event.getTimer().getGameTimeDeltaPartialTick(false));
                  }

                  renderer.visualUpdate(mainHandItem);
               }
            }
         }
      }
   }
}
