package com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderHandEvent;
import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation.IFPAnimationInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPGeoItemRenderer;
import com.tacz.guns.client.render26.GeoItemRegistry;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Tracks which animated first-person item is currently in the player's hand, plays its draw / put away
 * animations and renders it in place of the vanilla held item.
 */
@Environment(EnvType.CLIENT)
public class FirstPersonRenderHandler {
   private static int realSelectedSlot = -1;
   private static ItemStack realMainHand = ItemStack.EMPTY;
   private static boolean transitioning = false;
   private static IFPAnimationInstance activeInstance = null;
   private static IFPAnimationInstance previousInstance = null;
   private static ItemStack pendingTarget = ItemStack.EMPTY;
   private static long switchStartTime = 0L;
   private static long currentSheatheDuration = 0L;
   private static boolean lockVanilla = false;
   private static boolean nextIsCustom = false;
   private static boolean forceHandSwapFlag = false;

   public static void reset() {
      realSelectedSlot = -1;
      realMainHand = ItemStack.EMPTY;
      transitioning = false;
      activeInstance = null;
      previousInstance = null;
      pendingTarget = ItemStack.EMPTY;
      lockVanilla = false;
      nextIsCustom = false;
      forceHandSwapFlag = false;
      switchStartTime = 0L;
      currentSheatheDuration = 0L;
   }

   public static void onSwapItemWithOffHand() {
      forceHandSwapFlag = true;
   }

   public static void onClientTick(Minecraft client) {
      LocalPlayer player = client.player;
      if (player == null) {
         return;
      }

      int newSlot = player.getInventory().getSelectedSlot();
      ItemStack newMain = player.getMainHandItem();
      boolean slotChanged = newSlot != realSelectedSlot || forceHandSwapFlag;
      boolean itemChanged = !isSameItemStacks(realMainHand, newMain);
      forceHandSwapFlag = false;
      realSelectedSlot = newSlot;
      realMainHand = newMain;
      if (slotChanged || itemChanged) {
         onItemChanged(newMain);
      }

      if (activeInstance != null) {
         activeInstance.updateItem(newMain);
      }

      tickStates();
   }

   private static void onItemChanged(ItemStack newStack) {
      pendingTarget = newStack;
      nextIsCustom = isCustomItem(newStack);
      if (!transitioning) {
         boolean oldIsCustom = activeInstance != null;
         previousInstance = activeInstance;
         activeInstance = createInstance(newStack);
         if (oldIsCustom) {
            transitioning = true;
            lockVanilla = true;
            switchStartTime = System.currentTimeMillis();
            currentSheatheDuration = calculateSheatheDuration(previousInstance.currentItem());
            previousInstance.triggerPutAway();
         } else {
            transitioning = false;
            lockVanilla = false;
         }
      }
   }

   private static void tickStates() {
      if (transitioning && getSheatheProgress() >= 1.0F) {
         transitioning = false;
         lockVanilla = false;
         activeInstance = createInstance(pendingTarget);
         previousInstance = null;
      }
   }

   public static void tickAnimation(RenderTickEvent event) {
      if (event.phase == RenderTickEvent.Phase.START) {
         IFPAnimationInstance instance = getActiveAnimationInstance();
         if (instance != null) {
            instance.triggerDraw();
            instance.tick(event.getTimer().getGameTimeDeltaPartialTick(true));
         }
      }
   }

   public static void onRenderHand(RenderHandEvent event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return;
      }

      IFPAnimationInstance instance = getActiveAnimationInstance();
      if (instance == null) {
         return;
      }

      ItemStack stack = instance.currentItem();
      if (stack.isEmpty()) {
         return;
      }

      Optional<IFPGeoItemRenderer> renderer = getRenderer(stack);
      if (renderer.isEmpty()) {
         return;
      }

      if (event.getHand() == InteractionHand.OFF_HAND && renderer.get().blockOffhandRender()) {
         event.setCanceled(true);
      }

      renderer.get().renderFirstPerson(
         player, stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick()
      );
      event.setCanceled(true);
   }

   public static boolean shouldLockVanilla() {
      return lockVanilla;
   }

   public static float getTargetHeight() {
      return nextIsCustom ? 1.0F : 0.0F;
   }

   public static IFPAnimationInstance getActiveAnimationInstance() {
      return transitioning ? previousInstance : activeInstance;
   }

   private static IFPAnimationInstance createInstance(ItemStack stack) {
      return getRenderer(stack).map(r -> r.createAnimationInstance(stack, Minecraft.getInstance().getCameraEntity())).orElse(null);
   }

   private static boolean isCustomItem(ItemStack stack) {
      return getRenderer(stack).isPresent();
   }

   private static long calculateSheatheDuration(ItemStack stack) {
      return getRenderer(stack).map(r -> r.getPutAwayDuration(stack)).orElse(0L);
   }

   private static float getSheatheProgress() {
      if (currentSheatheDuration <= 0L) {
         return 1.0F;
      }

      long elapsed = System.currentTimeMillis() - switchStartTime;
      return Math.min(1.0F, (float)elapsed / (float)currentSheatheDuration);
   }

   private static Optional<IFPGeoItemRenderer> getRenderer(ItemStack stack) {
      if (stack.isEmpty()) {
         return Optional.empty();
      }

      return GeoItemRegistry.get(stack.getItem()) instanceof IFPGeoItemRenderer renderer ? Optional.of(renderer) : Optional.empty();
   }

   private static boolean isSameItemStacks(ItemStack oldStack, ItemStack newStack) {
      if (oldStack == newStack) {
         return true;
      } else if (oldStack.isEmpty() && newStack.isEmpty()) {
         return true;
      } else {
         return !oldStack.isEmpty() && !newStack.isEmpty()
            ? getRenderer(oldStack).map(r -> r.isSameItem(oldStack, newStack)).orElseGet(() -> ItemStack.matches(oldStack, newStack))
            : false;
      }
   }
}
