package com.tacz.guns.entity;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.extension.IEntityPersistentData;
import cn.sh1rocu.tacz.util.forge.network.IEntityExtension;
import cn.sh1rocu.tacz.util.forge.network.IEntityWithComplexSpawn;
import com.google.common.collect.Lists;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ITargetEntity;
import com.tacz.guns.api.entity.KnockBackModifier;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.client.particle.AmmoParticleSpawner;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.init.ModDamageTypes;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunHurt;
import com.tacz.guns.network.message.event.ServerMessageGunKill;
import com.tacz.guns.particles.BulletHoleOption;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.DamageModifier;
import com.tacz.guns.resource.modifier.custom.ExplosionModifier;
import com.tacz.guns.resource.modifier.custom.IgniteModifier;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import com.tacz.guns.util.EntityUtil;
import com.tacz.guns.util.ExplodeUtil;
import com.tacz.guns.util.TacHitResult;
import com.tacz.guns.util.block.BlockRayTrace;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.minecraft.world.phys.HitResult.Type;

import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.ClipContext.Block;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class EntityKineticBullet extends Projectile implements IEntityWithComplexSpawn, IEntityExtension {
   public static final EntityType<EntityKineticBullet> TYPE = net.minecraft.world.entity.EntityType.Builder.<EntityKineticBullet>of(EntityKineticBullet::new, MobCategory.MISC)
      .noSummon()
      .noSave()
      .fireImmune()
      .sized(0.0625F, 0.0625F)
      .clientTrackingRange(5)
      .updateInterval(5)
      .alwaysUpdateVelocity(false)
      .build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.Identifier.fromNamespaceAndPath("tacz", "bullet")));
   public static final TagKey<EntityType<?>> USE_MAGIC_DAMAGE_ON = TagKey.create(
      Registries.ENTITY_TYPE, Identifier.parse("tacz:use_magic_damage_on")
   );
   public static final TagKey<EntityType<?>> USE_VOID_DAMAGE_ON = TagKey.create(
      Registries.ENTITY_TYPE, Identifier.parse("tacz:use_void_damage_on")
   );
   public static final TagKey<EntityType<?>> PRETEND_MELEE_DAMAGE_ON = TagKey.create(
      Registries.ENTITY_TYPE, Identifier.parse("tacz:pretend_melee_damage_on")
   );
   public static final String TRACER_COLOR_OVERRIDER_KEY = "tacz:tracer_override";
   public static final String TRACER_SIZE_OVERRIDER_KEY = "tacz:tracer_size";
   private static final ExplosionData DEFAULT_EXPLOSION_DATA = new ExplosionData(false, 0.0F, 0.0F, false, 30.0F, false);
   private Identifier ammoId = DefaultAssets.EMPTY_AMMO_ID;
   private int life = 200;
   @Deprecated
   private float speed = 1.0F;
   private float gravity = 0.0F;
   private float friction = 0.01F;
   private LinkedList<ExtraDamage.DistanceDamagePair> damageAmount = Lists.newLinkedList();
   private float distanceAmount = 0.0F;
   private float knockback = 0.0F;
   private boolean explosion = false;
   private boolean igniteEntity = false;
   private boolean igniteBlock = false;
   private int igniteEntityTime = 2;
   private float explosionDamage = 3.0F;
   private float explosionRadius = 3.0F;
   private int explosionDelayCount = Integer.MAX_VALUE;
   private boolean explosionKnockback = false;
   private boolean explosionDestroyBlock = false;
   private float damageModifier = 1.0F;
   private int pierce = 1;
   private Vec3 startPos;
   private boolean isTracerAmmo;
   private float cameraXRot;
   private float cameraYRot;
   private Vector3f firstPersonRenderOffset;
   private Identifier gunId = DefaultAssets.EMPTY_GUN_ID;
   private Identifier gunDisplayId = DefaultAssets.DEFAULT_GUN_DISPLAY_ID;
   private float armorIgnore;
   private float headShot;
   private float shotDamageMultiplier = 1.0F;

   public EntityKineticBullet(EntityType<? extends Projectile> type, Level worldIn) {
      super(type, worldIn);
   }

   public EntityKineticBullet(EntityType<? extends Projectile> type, double x, double y, double z, Level worldIn) {
      this(type, worldIn);
      this.setPos(x, y, z);
   }

   public EntityKineticBullet(
      Level worldIn,
      LivingEntity throwerIn,
      ItemStack gunItem,
      Identifier ammoId,
      Identifier gunId,
      Identifier gunDisplayId,
      boolean isTracerAmmo,
      GunData gunData,
      BulletData bulletData
   ) {
      this(TYPE, worldIn, throwerIn, gunItem, ammoId, gunId, gunDisplayId, isTracerAmmo, gunData, bulletData);
   }

   public EntityKineticBullet(
      Level worldIn,
      LivingEntity throwerIn,
      ItemStack gunItem,
      Identifier ammoId,
      Identifier gunId,
      boolean isTracerAmmo,
      GunData gunData,
      BulletData bulletData
   ) {
      this(TYPE, worldIn, throwerIn, gunItem, ammoId, gunId, DefaultAssets.DEFAULT_GUN_DISPLAY_ID, isTracerAmmo, gunData, bulletData);
   }

   protected EntityKineticBullet(
      EntityType<? extends Projectile> type,
      Level worldIn,
      LivingEntity throwerIn,
      ItemStack gunItem,
      Identifier ammoId,
      Identifier gunId,
      Identifier gunDisplayId,
      boolean isTracerAmmo,
      GunData gunData,
      BulletData bulletData
   ) {
      this(type, throwerIn.getX(), throwerIn.getEyeY() - 0.1F, throwerIn.getZ(), worldIn);
      this.setOwner(throwerIn);
      this.gunId = gunId;
      AttachmentCacheProperty cacheProperty = Objects.requireNonNull(IGunOperator.fromLivingEntity(throwerIn).getCacheProperty());
      float armorIgnore = this.modifyProperty(GunProperties.ARMOR_IGNORE, Float.class, cacheProperty.getCache(GunProperties.ARMOR_IGNORE));
      float headshot = this.modifyProperty(GunProperties.HEADSHOT_MULTIPLIER, Float.class, cacheProperty.getCache(GunProperties.HEADSHOT_MULTIPLIER));
      float knockback = this.modifyProperty(GunProperties.KNOCKBACK, Float.class, cacheProperty.getCache(GunProperties.KNOCKBACK));
      this.armorIgnore = Mth.clamp(armorIgnore, 0.0F, 1.0F);
      this.headShot = Math.max(headshot, 0.0F);
      this.knockback = Math.max(knockback, 0.0F);
      this.ammoId = ammoId;
      float lifeSecond = this.modifyProperty("bullet_life", Float.class, bulletData.getLifeSecond());
      this.life = Mth.clamp((int)(lifeSecond * 20.0F), 1, Integer.MAX_VALUE);
      this.gravity = Mth.clamp(this.modifyProperty("bullet_gravity", Float.class, bulletData.getGravity()), 0.0F, Float.MAX_VALUE);
      this.friction = Mth.clamp(this.modifyProperty("bullet_friction", Float.class, bulletData.getFriction()), 0.0F, Float.MAX_VALUE);
      Ignite ignite = cacheProperty.getCache(IgniteModifier.ID);
      this.igniteEntity = this.modifyProperty("ignite_entity", Boolean.class, bulletData.getIgnite().isIgniteEntity() || ignite.isIgniteEntity());
      this.igniteEntityTime = Math.max(this.modifyProperty("ignite_entity_time", Integer.class, bulletData.getIgniteEntityTime()), 0);
      this.igniteBlock = this.modifyProperty("ignite_block", Boolean.class, bulletData.getIgnite().isIgniteBlock() || ignite.isIgniteBlock());
      this.damageAmount = cacheProperty.getCache(DamageModifier.ID);
      this.distanceAmount = this.modifyProperty(GunProperties.EFFECTIVE_RANGE, Float.class, cacheProperty.getCache(GunProperties.EFFECTIVE_RANGE));
      int pierce = this.modifyProperty(GunProperties.PIERCE, Integer.class, cacheProperty.getCache(GunProperties.PIERCE));
      this.pierce = Mth.clamp(pierce, 1, Integer.MAX_VALUE);
      ExplosionData explosionData = Objects.requireNonNullElse(cacheProperty.getCache(ExplosionModifier.ID), DEFAULT_EXPLOSION_DATA);
      this.explosion = this.modifyProperty("explode_enabled", Boolean.class, explosionData.isExplode());
      if (this.explosion) {
         Float explosionDamage = this.modifyProperty("explosion_damage", Float.class, explosionData.getDamage());
         Float explosionRadius = this.modifyProperty("explosion_radius", Float.class, explosionData.getRadius());
         this.explosionDamage = (float)Mth.clamp(
            explosionDamage.floatValue() * (Double)SyncConfig.DAMAGE_BASE_MULTIPLIER.get(), 0.0, Float.MAX_VALUE
         );
         this.explosionRadius = Mth.clamp(explosionRadius, 0.0F, Float.MAX_VALUE);
         this.explosionKnockback = this.modifyProperty("explosion_knockback", Boolean.class, explosionData.isKnockback());
         int delayTickCount = (int)(this.modifyProperty("explosion_delay", Float.class, explosionData.getDelay()) * 20.0F);
         if (delayTickCount < 0) {
            delayTickCount = Integer.MAX_VALUE;
         }

         this.explosionDestroyBlock = (Boolean)AmmoConfig.EXPLOSIVE_AMMO_DESTROYS_BLOCK.get()
            && this.modifyProperty("explosion_destroys_block", Boolean.class, explosionData.isDestroyBlock());
         this.explosionDelayCount = Math.max(delayTickCount, 1);
      }

      double posX = throwerIn.xOld + (throwerIn.getX() - throwerIn.xOld) / 2.0;
      double posY = throwerIn.yOld + (throwerIn.getY() - throwerIn.yOld) / 2.0 + throwerIn.getEyeHeight();
      double posZ = throwerIn.zOld + (throwerIn.getZ() - throwerIn.zOld) / 2.0;
      this.setPos(posX, posY, posZ);
      this.startPos = this.position();
      this.isTracerAmmo = isTracerAmmo;
      this.gunDisplayId = gunDisplayId;
   }

   @Internal
   public void applyShotgunDamageSpread(int bulletCount) {
      if (bulletCount > 1) {
         this.damageModifier = 1.0F / bulletCount;
      }
   }

   @Internal
   public void setShotDamageMultiplier(float multiplier) {
      this.shotDamageMultiplier = Math.max(multiplier, 0.0F);
   }

   protected void defineSynchedData(@NotNull net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
   }

   public void tick() {
      super.tick();
      this.onBulletTick();
      if (this.level().isClientSide()) {
         AmmoParticleSpawner.addParticle(this);
      }

      Vec3 movement = this.getDeltaMovement();
      double x = movement.x;
      double y = movement.y;
      double z = movement.z;
      double distance = movement.horizontalDistance();
      this.setYRot((float)Math.toDegrees(Mth.atan2(x, z)));
      this.setXRot((float)Math.toDegrees(Mth.atan2(y, distance)));
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }

      this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
      this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
      double nextPosX = this.getX() + x;
      double nextPosY = this.getY() + y;
      double nextPosZ = this.getZ() + z;
      this.setPos(nextPosX, nextPosY, nextPosZ);
      float friction = this.friction;
      float gravity = this.gravity;
      if (this.isInWater()) {
         for (int i = 0; i < 4; i++) {
            this.level().addParticle(ParticleTypes.BUBBLE, nextPosX - x * 0.25, nextPosY - y * 0.25, nextPosZ - z * 0.25, x, y, z);
         }

         friction = 0.4F;
         gravity *= 0.6F;
      }

      this.setDeltaMovement(this.getDeltaMovement().scale(1.0F - friction));
      this.setDeltaMovement(this.getDeltaMovement().add(0.0, -gravity, 0.0));
      if (this.tickCount >= this.life - 1) {
         this.discard();
      }
   }

   protected void onBulletTick() {
      if (!this.level().isClientSide()) {
         if (this.explosion) {
            if (this.explosionDelayCount <= 0) {
               ExplodeUtil.createExplosion(
                  this.getOwner(),
                  this,
                  this.explosionDamage,
                  this.explosionRadius,
                  this.explosionKnockback,
                  this.explosionDestroyBlock,
                  this.position()
               );
               this.discard();
               return;
            }

            this.explosionDelayCount--;
         }

         Vec3 startVec = this.position();
         Vec3 endVec = startVec.add(this.getDeltaMovement());
         HitResult result = BlockRayTrace.rayTraceBlocks(
            this.level(), new ClipContext(startVec, endVec, Block.COLLIDER, Fluid.NONE, this)
         );
         BlockHitResult resultB = (BlockHitResult)result;
         if (resultB.getType() != Type.MISS) {
            endVec = resultB.getLocation();
         }

         List<EntityKineticBullet.EntityResult> hitEntities = null;
         if (this.pierce > 1 && !this.explosion) {
            hitEntities = EntityUtil.findEntitiesOnPath(this, startVec, endVec);
         } else {
            EntityKineticBullet.EntityResult entityResult = EntityUtil.findEntityOnPath(this, startVec, endVec);
            if (entityResult != null) {
               hitEntities = Collections.singletonList(entityResult);
            }
         }

         if (hitEntities != null && !hitEntities.isEmpty()) {
            EntityKineticBullet.EntityResult[] hitEntityResult = hitEntities.toArray(new EntityKineticBullet.EntityResult[0]);

            for (int i = 0; (i < this.pierce || i < 1) && i < hitEntityResult.length - 1; i++) {
               int k = i;

               for (int j = i + 1; j < hitEntityResult.length; j++) {
                  if (hitEntityResult[j].hitVec.distanceTo(startVec) < hitEntityResult[k].hitVec.distanceTo(startVec)) {
                     k = j;
                  }
               }

               EntityKineticBullet.EntityResult t = hitEntityResult[i];
               hitEntityResult[i] = hitEntityResult[k];
               hitEntityResult[k] = t;
            }

            for (EntityKineticBullet.EntityResult entityResult : hitEntityResult) {
               TacHitResult var11 = new TacHitResult(entityResult);
               this.onHitEntity(var11, startVec, endVec);
               this.pierce--;
               if (this.pierce < 1 || this.explosion) {
                  this.discard();
                  return;
               }
            }
         }

         this.onHitBlock(resultB, startVec, endVec);
      }
   }

   public void shoot(double pitch, double yaw, float pVelocity, Vector2d vector2d) {
      Vector3d left = new Vector3d(vector2d.x, vector2d.y, 8.0);
      left.rotateX(pitch * (float) (Math.PI / 180.0));
      left.rotateY(-yaw * (float) (Math.PI / 180.0));
      Vec3 vec3 = new Vec3(left.x, left.y, left.z).normalize().scale(pVelocity);
      this.setDeltaMovement(vec3.x, vec3.y, vec3.z);
      double d0 = vec3.horizontalDistance();
      this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
      this.setXRot((float)(Mth.atan2(vec3.y, d0) * 180.0F / (float)Math.PI));
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   public void shootFromRotation(Entity pShooter, float pX, float pY, float pZ, float pVelocity, Vector2d vector2d) {
      this.shoot(pX, pY, pVelocity, vector2d);
      Vec3 vec3 = pShooter.getDeltaMovement();
      this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, pShooter.onGround() ? 0.0 : vec3.y, vec3.z));
   }

   protected void onHitEntity(TacHitResult result, Vec3 startVec, Vec3 endVec) {
      if (result.getEntity() instanceof ITargetEntity targetEntity) {
         DamageSource source = this.damageSources().thrown(this, this.getOwner());
         targetEntity.onProjectileHit(this, result, source, this.getDamage(result.getLocation()));
      } else {
         Entity entity = result.getEntity();
         Entity owner = this.getOwner();
         LivingEntity attacker = owner instanceof LivingEntity ? (LivingEntity)owner : null;
         Pair<DamageSource, DamageSource> sources = this.createDamageSources(EntityKineticBullet.MaybeMultipartEntity.of(entity));
         boolean headshot = result.isHeadshot();
         float damage = this.getDamage(result.getLocation());
         float headShotMultiplier = Math.max(this.headShot, 0.0F);
         EntityHurtByGunEvent.Pre preEvent = new EntityHurtByGunEvent.Pre(
            this, entity, attacker, this.gunId, this.gunDisplayId, damage, sources, headshot, headShotMultiplier, LogicalSide.SERVER
         );
         ((EntityHurtByGunEvent.PreCallBack)EntityHurtByGunEvent.PRE.invoker()).post(preEvent);
         if (!preEvent.isCanceled()) {
            entity = preEvent.getHurtEntity();
            EntityKineticBullet.MaybeMultipartEntity parts = EntityKineticBullet.MaybeMultipartEntity.of(entity);
            attacker = preEvent.getAttacker();
            Identifier newGunId = preEvent.getGunId();
            damage = preEvent.getBaseAmount();
            sources = Pair.of(preEvent.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), preEvent.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING));
            headshot = preEvent.isHeadShot();
            headShotMultiplier = preEvent.getHeadshotMultiplier();
            if (entity != null) {
               if (this.igniteEntity && (Boolean)AmmoConfig.IGNITE_ENTITY.get()) {
                  entity.setRemainingFireTicks(this.igniteEntityTime);
                  if (this.level() instanceof ServerLevel serverLevel) {
                     serverLevel.sendParticles(
                        ParticleTypes.LAVA,
                        entity.getX(),
                        entity.getY() + entity.getEyeHeight(),
                        entity.getZ(),
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0
                     );
                  }
               }

               if (headshot) {
                  damage *= headShotMultiplier;
               }

               if (parts.core() instanceof LivingEntity livingCore) {
                  KnockBackModifier modifier = KnockBackModifier.fromLivingEntity(livingCore);
                  modifier.setKnockBackStrength(this.knockback);
                  this.tacAttackEntity(parts, damage, sources);
                  modifier.resetKnockBackStrength();
               } else {
                  this.tacAttackEntity(parts, damage, sources);
               }

               if (this.explosion) {
                  parts.core().invulnerableTime = 0;
                  ExplodeUtil.createExplosion(
                     this.getOwner(),
                     this,
                     this.explosionDamage,
                     this.explosionRadius,
                     this.explosionKnockback,
                     this.explosionDestroyBlock,
                     result.getLocation()
                  );
               }

               if (parts.core() instanceof LivingEntity livingCore && !this.level().isClientSide()) {
                  int attackerId = attacker == null ? 0 : attacker.getId();
                  if (livingCore.isDeadOrDying()) {
                     EntityKillByGunEvent killByGunEvent = new EntityKillByGunEvent(
                        this, livingCore, attacker, newGunId, this.gunDisplayId, damage, sources, headshot, headShotMultiplier, LogicalSide.SERVER
                     );
                     ((EntityKillByGunEvent.Callback)EntityKillByGunEvent.CALLBACK.invoker()).post(killByGunEvent);
                     NetworkHandler.sendToDimension(
                        new ServerMessageGunKill(
                           this.getId(), livingCore.getId(), attackerId, newGunId, this.gunDisplayId, damage, headshot, headShotMultiplier
                        ),
                        livingCore
                     );
                  } else {
                     EntityHurtByGunEvent.Post hurtByGunEvent = new EntityHurtByGunEvent.Post(
                        this, livingCore, attacker, newGunId, this.gunDisplayId, damage, sources, headshot, headShotMultiplier, LogicalSide.SERVER
                     );
                     ((EntityHurtByGunEvent.PostCallBack)EntityHurtByGunEvent.POST.invoker()).post(hurtByGunEvent);
                     NetworkHandler.sendToDimension(
                        new ServerMessageGunHurt(
                           this.getId(), livingCore.getId(), attackerId, newGunId, this.gunDisplayId, damage, headshot, headShotMultiplier
                        ),
                        livingCore
                     );
                  }
               }
            }
         }
      }
   }

   protected void onHitBlock(BlockHitResult result, Vec3 startVec, Vec3 endVec) {
      if (result.getType() != Type.MISS) {
         BlockPos pos = result.getBlockPos();
         Vec3 hitVec = result.getLocation();
         AmmoHitBlockEvent ammoHitBlockEvent = new AmmoHitBlockEvent(this.level(), result, this.level().getBlockState(pos), this);
         ((AmmoHitBlockEvent.Callback)AmmoHitBlockEvent.CALLBACK.invoker()).post(ammoHitBlockEvent);
         if (!ammoHitBlockEvent.isCanceled()) {
            super.onHitBlock(result);
            if (this.explosion) {
               ExplodeUtil.createExplosion(
                  this.getOwner(), this, this.explosionDamage, this.explosionRadius, this.explosionKnockback, this.explosionDestroyBlock, hitVec
               );
               this.discard();
            } else {
               if (this.level() instanceof ServerLevel serverLevel) {
                  BulletHoleOption bulletHoleOption = new BulletHoleOption(
                     result.getDirection(), result.getBlockPos(), this.ammoId.toString(), this.gunId.toString(), this.gunDisplayId.toString()
                  );
                  serverLevel.sendParticles(bulletHoleOption, hitVec.x, hitVec.y, hitVec.z, 1, 0.0, 0.0, 0.0, 0.0);
                  if (this.igniteBlock) {
                     serverLevel.sendParticles(ParticleTypes.LAVA, hitVec.x, hitVec.y, hitVec.z, 1, 0.0, 0.0, 0.0, 0.0);
                  }
               }

               if (this.igniteBlock && (Boolean)AmmoConfig.IGNITE_BLOCK.get()) {
                  BlockPos offsetPos = pos.relative(result.getDirection());
                  if (BaseFireBlock.canBePlacedAt(this.level(), offsetPos, result.getDirection())) {
                     BlockState fireState = BaseFireBlock.getState(this.level(), offsetPos);
                     this.level().setBlock(offsetPos, fireState, 11);
                     ((ServerLevel)this.level())
                        .sendParticles(
                           ParticleTypes.LAVA,
                           hitVec.x - 1.0 + this.random.nextDouble() * 2.0,
                           hitVec.y,
                           hitVec.z - 1.0 + this.random.nextDouble() * 2.0,
                           4,
                           0.0,
                           0.0,
                           0.0,
                           0.0
                        );
                  }
               }

               this.discard();
            }
         }
      }
   }

   public float getDamage(Vec3 hitVec) {
      float base = 0.0F;
      double playerDistance = hitVec.distanceTo(this.startPos);

      for (ExtraDamage.DistanceDamagePair pair : this.damageAmount) {
         float effectiveDistance = this.damageAmount.getFirst().getDistance() == pair.getDistance() ? this.distanceAmount : pair.getDistance();
         if (playerDistance < effectiveDistance) {
            float damage = pair.getDamage();
            base = Math.max(damage * this.damageModifier, 0.0F);
            break;
         }
      }

      float modifiedDamage = this.modifyProperty(GunProperties.DAMAGE, Float.class, base);
      return Math.max(modifiedDamage * this.shotDamageMultiplier, 0.0F);
   }

   private <T> T modifyProperty(GunProperty<?> prop, Class<T> type, T original) {
      return this.modifyProperty(prop.name(), type, original);
   }

   private <T> T modifyProperty(String id, Class<T> type, T original) {
      if (this.getOwner() instanceof LivingEntity shooter) {
         ItemStack gun = shooter.getMainHandItem();
         if (gun.getItem() instanceof AbstractGunItem gunInterface && Objects.equals(this.gunId, gunInterface.getGunId(gun))) {
            ShooterDataHolder dataHolder = IGunOperator.fromLivingEntity(shooter).getDataHolder();
            return gunInterface.modifyProperty(dataHolder, gun, shooter, id, type, original);
         }
      }

      return original;
   }

   private Pair<DamageSource, DamageSource> createDamageSources(EntityKineticBullet.MaybeMultipartEntity parts) {
      EntityType<?> hitPartType = parts.hitPart().getType();
      Entity directCause = (Entity)(hitPartType.builtInRegistryHolder().is(PRETEND_MELEE_DAMAGE_ON) ? this.getOwner() : this);
      DamageSource source1;
      DamageSource source2;
      if (hitPartType.builtInRegistryHolder().is(USE_MAGIC_DAMAGE_ON)) {
         source1 = source2 = this.damageSources().indirectMagic(this, this.getOwner());
      } else if (hitPartType.builtInRegistryHolder().is(USE_VOID_DAMAGE_ON)) {
         source1 = ModDamageTypes.Sources.bulletVoid(this.registryAccess(), directCause, this.getOwner(), false);
         source2 = ModDamageTypes.Sources.bulletVoid(this.registryAccess(), directCause, this.getOwner(), true);
      } else {
         source1 = ModDamageTypes.Sources.bullet(this.registryAccess(), directCause, this.getOwner(), false);
         source2 = ModDamageTypes.Sources.bullet(this.registryAccess(), directCause, this.getOwner(), true);
      }

      return Pair.of(source1, source2);
   }

   private void tacAttackEntity(EntityKineticBullet.MaybeMultipartEntity parts, float damage, Pair<DamageSource, DamageSource> sources) {
      DamageSource source1 = (DamageSource)sources.getLeft();
      DamageSource source2 = (DamageSource)sources.getRight();
      float armorDamagePercent = Mth.clamp(this.armorIgnore, 0.0F, 1.0F);
      float normalDamagePercent = 1.0F - armorDamagePercent;
      parts.core().invulnerableTime = 0;
      parts.hitPart().hurt(source1, damage * normalDamagePercent);
      parts.core().invulnerableTime = 0;
      parts.hitPart().hurt(source2, damage * armorDamagePercent);
   }

   @Override
   public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
      buffer.writeFloat(this.getXRot());
      buffer.writeFloat(this.getYRot());
      buffer.writeDouble(this.getDeltaMovement().x);
      buffer.writeDouble(this.getDeltaMovement().y);
      buffer.writeDouble(this.getDeltaMovement().z);
      Entity entity = this.getOwner();
      buffer.writeInt(entity != null ? entity.getId() : 0);
      buffer.writeIdentifier(this.ammoId);
      buffer.writeFloat(this.gravity);
      buffer.writeBoolean(this.explosion);
      buffer.writeBoolean(this.igniteEntity);
      buffer.writeBoolean(this.igniteBlock);
      buffer.writeFloat(this.explosionRadius);
      buffer.writeFloat(this.explosionDamage);
      buffer.writeInt(this.life);
      buffer.writeFloat(this.speed);
      buffer.writeFloat(this.friction);
      buffer.writeInt(this.pierce);
      buffer.writeBoolean(this.isTracerAmmo);
      buffer.writeIdentifier(this.gunId);
      buffer.writeIdentifier(this.gunDisplayId);
   }

   @Override
   public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
      this.setXRot(additionalData.readFloat());
      this.setYRot(additionalData.readFloat());
      this.setDeltaMovement(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
      Entity entity = this.level().getEntity(additionalData.readInt());
      if (entity != null) {
         this.setOwner(entity);
      }

      this.ammoId = additionalData.readIdentifier();
      this.gravity = additionalData.readFloat();
      this.explosion = additionalData.readBoolean();
      this.igniteEntity = additionalData.readBoolean();
      this.igniteBlock = additionalData.readBoolean();
      this.explosionRadius = additionalData.readFloat();
      this.explosionDamage = additionalData.readFloat();
      this.life = additionalData.readInt();
      this.speed = additionalData.readFloat();
      this.friction = additionalData.readFloat();
      this.pierce = additionalData.readInt();
      this.isTracerAmmo = additionalData.readBoolean();
      this.gunId = additionalData.readIdentifier();
      this.gunDisplayId = additionalData.readIdentifier();
   }

   public Identifier getAmmoId() {
      return this.ammoId;
   }

   public Identifier getGunId() {
      return this.gunId;
   }

   public Identifier getGunDisplayId() {
      return this.gunDisplayId;
   }

   public boolean isTracerAmmo() {
      return this.isTracerAmmo;
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public float getCameraYRot() {
      return this.cameraYRot;
   }

   public void setCameraYRot(float cameraYRot) {
      this.cameraYRot = cameraYRot;
   }

   public float getCameraXRot() {
      return this.cameraXRot;
   }

   public void setCameraXRot(float cameraXRot) {
      this.cameraXRot = cameraXRot;
   }

   public Vector3f getFirstPersonRenderOffset() {
      return this.firstPersonRenderOffset;
   }

   public void setFirstPersonRenderOffset(Vector3f originRenderOffset) {
      this.firstPersonRenderOffset = originRenderOffset;
   }

   public Optional<float[]> getTracerColorOverride() {
      CompoundTag pd = ((IEntityPersistentData)this).tacz$getPersistentData();
      if (!pd.contains("tacz:tracer_override")) {
         return Optional.empty();
      }

      int[] ints = pd.getIntArray("tacz:tracer_override").orElse(new int[0]);
      switch (ints.length) {
         case 0:
            return Optional.empty();
         case 1: {
            float albedo = ints[0] / 255.0F;
            return Optional.of(new float[]{albedo, albedo, albedo, 1.0F});
         }
         case 2: {
            float albedo = ints[0] / 255.0F;
            float alpha = ints[1] / 255.0F;
            return Optional.of(new float[]{albedo, albedo, albedo, alpha});
         }
         case 3: {
            float r = ints[0] / 255.0F;
            float g = ints[1] / 255.0F;
            float b = ints[2] / 255.0F;
            return Optional.of(new float[]{r, g, b, 1.0F});
         }
         default: {
            float r = ints[0] / 255.0F;
            float g = ints[1] / 255.0F;
            float b = ints[2] / 255.0F;
            float a = ints[3] / 255.0F;
            return Optional.of(new float[]{r, g, b, a});
         }
      }
   }

   public float getTracerSizeOverride() {
      CompoundTag pd = ((IEntityPersistentData)this).tacz$getPersistentData();
      return pd.contains("tacz:tracer_size") ? pd.getFloatOr("tacz:tracer_size", 0.0F) : 1.0F;
   }

   public boolean ownedBy(@Nullable Entity entity) {
      return entity == null ? false : super.ownedBy(entity);
   }

   public static class EntityResult {
      private final Entity entity;
      private final Vec3 hitVec;
      private final boolean headshot;

      public EntityResult(Entity entity, Vec3 hitVec, boolean headshot) {
         this.entity = entity;
         this.hitVec = hitVec;
         this.headshot = headshot;
      }

      public Entity getEntity() {
         return this.entity;
      }

      public Vec3 getHitPos() {
         return this.hitVec;
      }

      public boolean isHeadshot() {
         return this.headshot;
      }
   }

   public record MaybeMultipartEntity(Entity hitPart, Entity core) {
      public static EntityKineticBullet.MaybeMultipartEntity of(Entity hitPart) {
         Entity core = hitPart;
         return new EntityKineticBullet.MaybeMultipartEntity(hitPart, core);
      }
   }
}
