package com.tacz.guns.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.client.gameplay.LocalPlayerAim;
import com.tacz.guns.client.gameplay.LocalPlayerBolt;
import com.tacz.guns.client.gameplay.LocalPlayerCrawl;
import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import com.tacz.guns.client.gameplay.LocalPlayerDraw;
import com.tacz.guns.client.gameplay.LocalPlayerFireSelect;
import com.tacz.guns.client.gameplay.LocalPlayerInspect;
import com.tacz.guns.client.gameplay.LocalPlayerMelee;
import com.tacz.guns.client.gameplay.LocalPlayerReload;
import com.tacz.guns.client.gameplay.LocalPlayerShoot;
import com.tacz.guns.client.gameplay.LocalPlayerSprint;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements IClientPlayerGunOperator {
   @Unique
   private final LocalPlayer tac$player = (LocalPlayer)(Object)this;
   @Unique
   private final LocalPlayerDataHolder tac$data = new LocalPlayerDataHolder(this.tac$player);
   @Unique
   private final LocalPlayerAim tac$aim = new LocalPlayerAim(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerCrawl tac$crawl = new LocalPlayerCrawl(this.tac$player);
   @Unique
   private final LocalPlayerBolt tac$bolt = new LocalPlayerBolt(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerDraw tac$draw = new LocalPlayerDraw(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerFireSelect tac$fireSelect = new LocalPlayerFireSelect(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerMelee tac$melee = new LocalPlayerMelee(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerInspect tac$inspect = new LocalPlayerInspect(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerReload tac$reload = new LocalPlayerReload(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerShoot tac$shoot = new LocalPlayerShoot(this.tac$data, this.tac$player);
   @Unique
   private final LocalPlayerSprint tac$sprint = new LocalPlayerSprint(this.tac$data, this.tac$player);

   @Unique
   @Override
   public ShootResult shoot() {
      this.tac$reload.cancelReload();
      return this.tac$shoot.shoot();
   }

   @Unique
   @Override
   public void draw(ItemStack lastItem) {
      this.tac$draw.draw(lastItem);
   }

   @Unique
   @Override
   public void bolt() {
      this.tac$bolt.bolt();
   }

   @Unique
   @Override
   public void reload() {
      this.tac$reload.reload();
   }

   @Unique
   @Override
   public void inspect() {
      this.tac$inspect.inspect();
   }

   @Override
   public void fireSelect() {
      this.tac$fireSelect.fireSelect();
   }

   @Override
   public void melee() {
      this.tac$melee.melee();
   }

   @Override
   public void aim(boolean isAim) {
      this.tac$aim.aim(isAim);
   }

   @Override
   public boolean isCrawl() {
      return this.tac$crawl.isCrawling();
   }

   @Override
   public LocalPlayerDataHolder getDataHolder() {
      return this.tac$data;
   }

   @Override
   public void crawl(boolean isCrawl) {
      this.tac$crawl.crawl(isCrawl);
   }

   @Unique
   @Override
   public float getClientAimingProgress(float partialTicks) {
      return this.tac$aim.getClientAimingProgress(partialTicks);
   }

   @Unique
   @Override
   public long getClientShootCoolDown() {
      return this.tac$shoot.getClientShootCoolDown();
   }

   @Inject(method = "tick", at = @At("HEAD"))
   public void onTickClientSide(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      if (player.level().isClientSide()) {
         this.tac$aim.tickAimingProgress();
         this.tac$crawl.tickCrawl();
         this.tac$data.tickStateLock();
         this.tac$bolt.tickAutoBolt();
         player.setSprinting(this.tac$sprint.getProcessedSprintStatus(player.isSprinting()));
      }
   }

   @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V"))
   public void swapSprintStatus(LocalPlayer player, boolean sprinting, Operation<Void> original) {
      if (sprinting) {
         this.tac$reload.cancelReload();
      }

      original.call(new Object[]{player, this.tac$sprint.getProcessedSprintStatus(sprinting)});
   }

   @Inject(method = "respawn", at = @At("RETURN"))
   public void onRespawn(CallbackInfo ci) {
      this.tac$data.reset();
      this.draw(ItemStack.EMPTY);
   }

   @Override
   public boolean isAim() {
      return this.tac$aim.isAim();
   }

   @Override
   public void resetDraw() {
      this.tac$draw.readyToDraw = false;
   }

   @Override
   public boolean isReadyToDraw() {
      return this.tac$draw.readyToDraw;
   }

   @Override
   public boolean chargeShoot(boolean isCharge) {
      return this.tac$shoot.chargeShoot(isCharge);
   }

   @Override
   public float getChargeProgress() {
      return this.tac$data.chargeProgress;
   }

   @Override
   public boolean isCharging() {
      return this.tac$data.isCharging;
   }
}
