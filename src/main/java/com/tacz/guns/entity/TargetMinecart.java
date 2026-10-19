package com.tacz.guns.entity;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.extension.IMinecart;
import com.tacz.guns.api.entity.ITargetEntity;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.common.OtherConfig;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.init.ModSounds;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunHurt;
import java.util.Optional;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TargetMinecart extends AbstractMinecart implements ITargetEntity, IMinecart {
   public static EntityType<TargetMinecart> TYPE = EntityType.Builder.<TargetMinecart>of(TargetMinecart::new, MobCategory.MISC)
      .sized(0.75F, 2.4F)
      .clientTrackingRange(8)
      .build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.Identifier.fromNamespaceAndPath("tacz", "target_minecart")));
   @Nullable
   private ResolvableProfile gameProfile = null;

   public TargetMinecart(EntityType<? extends TargetMinecart> type, Level world) {
      super(type, world);
   }

   public TargetMinecart(Level level, double x, double y, double z) {
      super(TYPE, level, x, y, z);
   }

   @Override
   public void onProjectileHit(Entity entity, EntityHitResult result, DamageSource source, float damage) {
      if (!this.level().isClientSide() && !this.isRemoved()) {
         if (!source.isDirect()) {
            Entity sourceEntity = source.getEntity();
            if (sourceEntity instanceof Player player) {
               this.setHurtDir(-1);
               this.setHurtTime(10);
               this.markHurt();
               this.setDamage(10.0F);
               double dis = this.position().distanceTo(sourceEntity.position());
               player.sendOverlayMessage(
                  Component.translatable("message.tacz.target_minecart.hit", new Object[]{String.format("%.1f", damage), String.format("%.2f", dis)})
               );
               float volume = ((Integer)OtherConfig.TARGET_SOUND_DISTANCE.get()).intValue() / 16.0F;
               volume = Math.max(volume, 0.0F);
               this.level()
                  .playSound(null, this, ModSounds.TARGET_HIT, SoundSource.BLOCKS, volume, this.level().getRandom().nextFloat() * 0.1F + 0.9F);
               if (entity instanceof EntityKineticBullet projectile) {
                  boolean isHeadshot = false;
                  float headshotMultiplier = 1.0F;
                  EntityHurtByGunEvent.Post event = new EntityHurtByGunEvent.Post(
                     projectile,
                     this,
                     player,
                     projectile.getGunId(),
                     projectile.getGunDisplayId(),
                     damage,
                     Pair.of(source, source),
                     isHeadshot,
                     headshotMultiplier,
                     LogicalSide.SERVER
                  );
                  ((EntityHurtByGunEvent.PostCallBack)EntityHurtByGunEvent.POST.invoker()).post(event);
                  NetworkHandler.sendToDimension(
                     new ServerMessageGunHurt(
                        projectile.getId(),
                        this.getId(),
                        player.getId(),
                        projectile.getGunId(),
                        projectile.getGunDisplayId(),
                        damage,
                        isHeadshot,
                        headshotMultiplier
                     ),
                     this
                  );
               }
            }
         }
      }
   }

   public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
      return !source.is(DamageTypeTags.IS_EXPLOSION) && super.hurtServer(level, source, amount);
   }

   @Override
   public boolean tacz$canBeRidden() {
      return false;
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      double size = this.getBoundingBox().getSize();
      if (Double.isNaN(size)) {
         size = 1.0;
      }

      size *= ((Integer)RenderConfig.TARGET_RENDER_DISTANCE.get()).intValue() * getViewScale();
      return distance < size * size;
   }

   public void destroy(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source) {
      this.remove(RemovalReason.KILLED);
      if (serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
         ItemStack itemStack = new ItemStack(ModItems.TARGET_MINECART);
         if (this.hasCustomName()) {
            itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
         }

         this.spawnAtLocation(serverLevel, itemStack);
      }
   }

   @Override
   public boolean isRideable() {
      return this.tacz$canBeRidden();
   }

   protected Item getDropItem() {
      return ModItems.TARGET_MINECART;
   }

   public ItemStack getPickResult() {
      ItemStack itemStack = new ItemStack(ModItems.TARGET_MINECART);
      if (this.hasCustomName()) {
         itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
      }

      return itemStack;
   }

   @Nullable
   public ResolvableProfile getGameProfile() {
      if (this.gameProfile == null && this.getCustomName() != null) {
         this.gameProfile = ResolvableProfile.createUnresolved(this.getCustomName().getString());
      }

      return this.gameProfile;
   }

   @NotNull
   public BlockState getDefaultDisplayBlockState() {
      return ModBlocks.TARGET.defaultBlockState();
   }

   protected double getMaxSpeed(net.minecraft.server.level.ServerLevel level) {
      return 0.2F;
   }
}
