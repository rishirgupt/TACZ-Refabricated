package com.tacz.guns.client.event;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderHandEvent;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.client.other.KeepingItemRenderer;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.compat.iris.IrisCompat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(EnvType.CLIENT)
public class FirstPersonRenderEvent {
   private static AnimationStateMachine<?> lastStateMachine = null;

   public static void onRenderHand(RenderHandEvent event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (stack.getItem() instanceof IGun) {
               event.setCanceled(true);
            }
         } else {
            ItemStack stack = event.getItemStack();
            ItemDisplayContext transformType;
            if (event.getHand() == InteractionHand.MAIN_HAND) {
               transformType = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
            } else {
               transformType = ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            }

            if (com.tacz.guns.client.render26.GeoItemRegistry.get(stack.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
               AnimationStateMachine<?> machine = renderer.getStateMachine(stack);
               if (machine != lastStateMachine) {
                  if (lastStateMachine != null && lastStateMachine.isInitialized()) {
                     lastStateMachine.exit();
                  }

                  lastStateMachine = machine;
               }

               boolean flag = ItemStack.matches(player.getMainHandItem(), stack);
               if (flag && renderer.needReInit(stack)) {
                  renderer.tryInit(stack, player, event.getPartialTick());
               }

               IrisCompat.endBatch(com.tacz.guns.client.render26.GunRender.bufferSource());
               renderer.renderFirstPerson(
                  player, stack, transformType, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick()
               );
               event.setCanceled(true);
            }
         }
      }
   }
}
