package com.tacz.guns.client.animation.statemachine;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.util.LuaNbtAccessor;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;
import org.luaj.vm2.LuaTable;

public class GunAnimationStateContext extends ItemAnimationStateContext {
   private ItemStack currentGunItem;
   private IGun iGun;
   private GunDisplayInstance display;
   private GunData gunData;
   private float walkDistAnchor = 0.0F;
   private LuaNbtAccessor nbtUtil;

   private <T> Optional<T> processGunData(BiFunction<IGun, GunDisplayInstance, T> processor) {
      return this.iGun != null && this.display != null ? Optional.ofNullable(processor.apply(this.iGun, this.display)) : Optional.empty();
   }

   private <T> Optional<T> processGunOperator(Function<IClientPlayerGunOperator, T> processor) {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null ? Optional.ofNullable(processor.apply(IClientPlayerGunOperator.fromLocalPlayer(player))) : Optional.empty();
   }

   private <T> Optional<T> processRemoteGunOperator(Function<IGunOperator, T> processor) {
      return this.processCameraEntity(entity -> {
         if (entity instanceof LivingEntity) {
            IGunOperator gunOperator = IGunOperator.fromLivingEntity((LivingEntity)entity);
            return processor.apply(gunOperator);
         } else {
            return null;
         }
      });
   }

   private <T> Optional<T> processCameraEntity(Function<Entity, T> processor) {
      Entity entity = Minecraft.getInstance().getCameraEntity();
      return entity != null ? Optional.ofNullable(processor.apply(entity)) : Optional.empty();
   }

   public boolean hasBulletInBarrel() {
      return this.<Boolean>processGunData((iGun, gunIndex) -> {
         Bolt boltType = this.gunData.getBolt();
         return boltType != Bolt.OPEN_BOLT && iGun.hasBulletInBarrel(this.currentGunItem);
      }).orElse(false);
   }

   public boolean isOverHeat() {
      return this.gunData.getHeatData() != null && this.iGun.isOverheatLocked(this.currentGunItem);
   }

   public float getHeatProgress() {
      return this.gunData.getHeatData() != null
         ? Mth.clamp(this.iGun.getHeatAmount(this.currentGunItem) / this.gunData.getHeatData().getHeatMax(), 0.0F, 1.0F)
         : 0.0F;
   }

   public long getShootInterval() {
      return this.<Long>processCameraEntity(entity -> {
         if (entity instanceof LivingEntity livingEntity) {
            FireMode fireMode = this.iGun.getFireMode(this.currentGunItem);
            if (fireMode == FireMode.BURST) {
               long coolDown = (long)(this.gunData.getBurstData().getMinInterval() * 1000.0);
               return Math.max(coolDown, 0L);
            } else {
               long coolDown = this.gunData.getShootInterval(livingEntity, fireMode, this.currentGunItem);
               return Math.max(coolDown, 0L);
            }
         } else {
            return 0L;
         }
      }).orElse(0L);
   }

   public long getLastShootTimestamp() {
      return this.<Long>processGunOperator(operator -> operator.getDataHolder().clientLastShootTimestamp).orElse(-1L);
   }

   public long getCurrentTimestamp() {
      return System.currentTimeMillis();
   }

   public void adjustClientShootInterval(long alpha) {
      this.processGunOperator(operator -> {
         long timestamp = operator.getDataHolder().clientShootTimestamp;
         operator.getDataHolder().clientShootTimestamp = timestamp + alpha;
         return null;
      });
   }

   public int getAmmoCount() {
      return this.<Integer>processGunData((iGun, gunIndex) -> iGun.getCurrentAmmoCount(this.currentGunItem)).orElse(0);
   }

   public int getMaxAmmoCount() {
      return this.<Integer>processGunData((iGun, gunIndex) -> AttachmentDataUtils.getAmmoCountWithAttachment(this.currentGunItem, this.gunData)).orElse(0);
   }

   public boolean hasAmmoToConsume() {
      if (!this.processRemoteGunOperator(IGunOperator::needCheckAmmo).orElse(true)) {
         return true;
      } else {
         return this.iGun.useDummyAmmo(this.currentGunItem)
            ? this.iGun.getDummyAmmoAmount(this.currentGunItem) > 0
            : this.<Boolean>processCameraEntity(entity -> entity instanceof LivingEntity livingEntity ? livingEntity.tacz$getItemHandler(null).map(cap -> {
               for (int i = 0; i < cap.getSlots(); i++) {
                  ItemStack checkAmmoStack = cap.getStackInSlot(i);
                  if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(this.currentGunItem, checkAmmoStack)) {
                     return true;
                  }

                  if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(this.currentGunItem, checkAmmoStack)) {
                     return true;
                  }
               }

               return false;
            }).orElse(false) : false).orElse(false);
      }
   }

   public int getMagExtentLevel() {
      return this.<Integer>processGunData((iGun, gunIndex) -> AttachmentDataUtils.getMagExtendLevel(this.currentGunItem, this.gunData)).orElse(0);
   }

   public int getFireMode() {
      return this.<Integer>processGunData((iGun, gunIndex) -> iGun.getFireMode(this.currentGunItem).ordinal()).orElse(0);
   }

   public float getAimingProgress() {
      return this.<Float>processGunOperator(operator -> operator.getClientAimingProgress(this.partialTicks)).orElse(0.0F);
   }

   public boolean isAiming() {
      return this.processGunOperator(IClientPlayerGunOperator::isAim).orElse(false);
   }

   public long getShootCoolDown() {
      return this.processGunOperator(IClientPlayerGunOperator::getClientShootCoolDown).orElse(0L);
   }

   public int getReloadStateType() {
      return this.<Integer>processCameraEntity(
            entity -> entity instanceof LivingEntity livingEntity
               ? IGunOperator.fromLivingEntity(livingEntity).getSynReloadState().getStateType().ordinal()
               : ReloadState.StateType.NOT_RELOADING.ordinal()
         )
         .orElse(ReloadState.StateType.NOT_RELOADING.ordinal());
   }

   public boolean isInputUp() {
      return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.forward()).orElse(false);
   }

   public boolean isInputDown() {
      return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.backward()).orElse(false);
   }

   public boolean isInputLeft() {
      return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.left()).orElse(false);
   }

   public boolean isInputRight() {
      return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.right()).orElse(false);
   }

   public boolean isInputJumping() {
      return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.jump()).orElse(false);
   }

   public boolean isCrawl() {
      return this.processGunOperator(IClientPlayerGunOperator::isCrawl).orElse(false);
   }

   public boolean isOnGround() {
      return this.<Boolean>processCameraEntity(Entity::onGround).orElse(false);
   }

   public boolean isCrouching() {
      return this.<Boolean>processCameraEntity(Entity::isCrouching).orElse(false);
   }

   public boolean shouldSlide() {
      return this.<Boolean>processCameraEntity(e -> e.isCrouching() && this.gunData.canSlide()).orElse(false);
   }

   private static float walkDistOf(Entity entity, float partialTicks) {
      if (entity instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer) {
         return clientPlayer.avatarState().getInterpolatedWalkDistance(partialTicks);
      }

      return entity.moveDist;
   }

   public void anchorWalkDist() {
      this.processCameraEntity(entity -> {
         this.walkDistAnchor = walkDistOf(entity, this.partialTicks);
         return null;
      });
   }

   public float getWalkDist() {
      return this.<Float>processCameraEntity(entity -> {
         float currentWalkDist = walkDistOf(entity, this.partialTicks);
         return currentWalkDist - this.walkDistAnchor;
      }).orElse(0.0F);
   }

   public void popShellFrom(int index) {
      if (this.display.getShellEjection() != null) {
         BedrockGunModel gunModel = this.display.getGunModel();
         if (gunModel != null) {
            ShellRender shellRender = gunModel.getShellRender(index);
            Vector3f velocity = this.display.getShellEjection().getRandomVelocity();
            if (shellRender != null) {
               shellRender.addShell(velocity);
            }

            Pair<BedrockGunModel, Identifier> lod = this.display.getLodModel();
            if (lod != null) {
               ShellRender lodShell = ((BedrockGunModel)lod.getLeft()).getShellRender(index);
               if (lodShell != null) {
                  lodShell.addShell(velocity);
               }
            }
         }
      }
   }

   public LuaTable getStateMachineParams() {
      LuaTable param = this.display.getStateMachineParam();
      return param == null ? new LuaTable() : param;
   }

   public LuaNbtAccessor getNbtAccessor() {
      return this.nbtUtil;
   }

   public String getAttachment(String type) {
      try {
         AttachmentType t = AttachmentType.valueOf(type);
         return this.iGun.getAttachmentId(this.currentGunItem, t).toString();
      } catch (IllegalArgumentException e) {
         return DefaultAssets.EMPTY_ATTACHMENT_ID.toString();
      }
   }

   public float getChargeProgress() {
      return this.processGunOperator(IClientPlayerGunOperator::getChargeProgress).orElse(0.0F);
   }

   public float getMaxCharge() {
      return this.<Float>processGunData((iGun, gunIndex) -> {
         ChargeData chargeData = this.gunData.getChargeData(iGun.getFireMode(this.currentGunItem));
         return chargeData != null ? chargeData.getMaxCharge() : 0.0F;
      }).orElse(0.0F);
   }

   public float getChargeThreshold() {
      return this.<Float>processGunData((iGun, gunIndex) -> {
         ChargeData chargeData = this.gunData.getChargeData(iGun.getFireMode(this.currentGunItem));
         return chargeData != null ? chargeData.getFireThreshold() : 0.0F;
      }).orElse(0.0F);
   }

   public boolean isCharging() {
      return this.processGunOperator(IClientPlayerGunOperator::isCharging).orElse(false);
   }

   public void setCurrentGunItem(ItemStack currentGunItem) {
      this.currentGunItem = currentGunItem;
      this.iGun = IGun.getIGunOrNull(currentGunItem);
      if (this.iGun != null) {
         this.display = TimelessAPI.getGunDisplay(currentGunItem).orElse(null);
         this.gunData = TimelessAPI.getClientGunIndex(this.iGun.getGunId(currentGunItem)).map(ClientGunIndex::getGunData).orElse(null);
      }

      if (currentGunItem.has(DataComponents.CUSTOM_DATA)) {
         this.nbtUtil = new LuaNbtAccessor(((CustomData)currentGunItem.get(DataComponents.CUSTOM_DATA)).copyTag());
      }
   }
}
