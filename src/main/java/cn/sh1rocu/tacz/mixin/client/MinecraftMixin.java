package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.event.AddPackFindersEvent;
import cn.sh1rocu.tacz.api.event.InputEvent;
import cn.sh1rocu.tacz.api.mixin.PackRepositoryExtension;
import cn.sh1rocu.tacz.util.forge.ClientHooks;
import javax.annotation.Nullable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
   @Shadow
   @Final
   public Options options;
   @Shadow
   @Nullable
   public MultiPlayerGameMode gameMode;
   @Shadow
   @Nullable
   public LocalPlayer player;

   @Shadow
   public abstract PackRepository getResourcePackRepository();

   @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"))
   private void tacz$addPacks(GameConfig gameConfig, CallbackInfo ci) {
      AddPackFindersEvent event = new AddPackFindersEvent(
         PackType.CLIENT_RESOURCES, ((PackRepositoryExtension)this.getResourcePackRepository())::tacz$addPackFinder, false
      );
      ((AddPackFindersEvent.Callback)AddPackFindersEvent.CALLBACK.invoker()).onAddPackFinders(event);
   }

   @Inject(method = "clearClientLevel", at = @At("HEAD"))
   private void tacz$disconnect(Screen screen, CallbackInfo ci) {
      ClientHooks.firePlayerLogout(this.gameMode, this.player);
   }

   @Unique
   private InputEvent.InteractionKeyMappingTriggered tacz$onClickInput(int button, KeyMapping keyMapping, InteractionHand hand) {
      InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(button, keyMapping, hand);
      ((InputEvent.IKMTCallback)InputEvent.InteractionKeyMappingTriggered.EVENT.invoker()).onInteractionKeyMappingTriggered(event);
      return event;
   }

   @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
   private void tacz$onContinueAttack(boolean leftClick, CallbackInfo ci) {
      if (!leftClick || this.player == null) {
         return;
      }

      InputEvent.InteractionKeyMappingTriggered event = this.tacz$onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
      if (event.isCanceled()) {
         if (event.shouldSwingHand()) {
            this.player.swing(InteractionHand.MAIN_HAND);
         }

         ci.cancel();
      }
   }

   @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
   private void tacz$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
      if (this.player == null) {
         return;
      }

      InputEvent.InteractionKeyMappingTriggered event = this.tacz$onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
      if (event.isCanceled()) {
         if (event.shouldSwingHand()) {
            this.player.swing(InteractionHand.MAIN_HAND);
         }

         cir.setReturnValue(false);
      }
   }

   @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
   private void tacz$onStartUseItem(CallbackInfo ci) {
      if (this.player == null) {
         return;
      }

      InputEvent.InteractionKeyMappingTriggered event = this.tacz$onClickInput(1, this.options.keyUse, InteractionHand.MAIN_HAND);
      if (event.isCanceled()) {
         if (event.shouldSwingHand()) {
            this.player.swing(InteractionHand.MAIN_HAND);
         }

         ci.cancel();
      }
   }

   @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
   private void tacz$callInteractionPickInput(CallbackInfo ci) {
      if (this.tacz$onClickInput(2, this.options.keyPickItem, InteractionHand.MAIN_HAND).isCanceled()) {
         ci.cancel();
      }
   }
}
