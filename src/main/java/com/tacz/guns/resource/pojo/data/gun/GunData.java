package com.tacz.guns.resource.pojo.data.gun;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.RpmModifier;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class GunData {
   @SerializedName("ammo")
   private Identifier ammoId = null;
   @SerializedName("ammo_amount")
   private int ammoAmount = 30;
   @SerializedName("extended_mag_ammo_amount")
   private int @Nullable [] extendedMagAmmoAmount = null;
   @SerializedName("can_crawl")
   private boolean canCrawl = true;
   @SerializedName("can_slide")
   private boolean canSlide = true;
   @SerializedName("bolt")
   private Bolt bolt = Bolt.OPEN_BOLT;
   @SerializedName("rpm")
   private int roundsPerMinute = 300;
   @SerializedName("bullet")
   private BulletData bulletData = new BulletData();
   @SerializedName("draw_time")
   private float drawTime = 0.4F;
   @SerializedName("put_away_time")
   private float putAwayTime = 0.4F;
   @SerializedName("sprint_time")
   private float sprintTime = 0.2F;
   @SerializedName("aim_time")
   private float aimTime = 0.2F;
   @SerializedName("bolt_action_time")
   private float boltActionTime = 0.0F;
   @SerializedName("bolt_feed_time")
   private float boltFeedTime = -1.0F;
   @SerializedName("fire_sound")
   private FireSound fireSound = new FireSound();
   @SerializedName("reload")
   private GunReloadData reloadData = new GunReloadData();
   @SerializedName("fire_mode")
   private List<FireMode> fireModeSet = Collections.singletonList(FireMode.UNKNOWN);
   @SerializedName("fire_mode_adjust")
   private EnumMap<FireMode, GunFireModeAdjustData> fireModeAdjust = Maps.newEnumMap(FireMode.class);
   @SerializedName("burst_data")
   private BurstData burstData = new BurstData();
   @SerializedName("crawl_recoil_multiplier")
   private float crawlRecoilMultiplier = 0.5F;
   @SerializedName("recoil")
   private GunRecoil recoil = new GunRecoil();
   @SerializedName("hurt_bob_tweak_multiplier")
   private float hurtBobTweakMultiplier = 0.05F;
   @SerializedName("inaccuracy")
   private Map<InaccuracyType, Float> inaccuracy = null;
   @SerializedName("movement_speed")
   private MoveSpeed moveSpeed = new MoveSpeed();
   @SerializedName("melee")
   private GunMeleeData gunMeleeData = new GunMeleeData();
   @SerializedName("heat")
   @Nullable
   private GunHeatData gunHeatData = null;
   @SerializedName("allow_attachment_types")
   private List<AttachmentType> allowAttachments = Lists.newArrayList();
   @SerializedName("exclusive_attachments")
   private Map<Identifier, AttachmentData> exclusiveAttachments = Maps.newHashMap();
   @SerializedName("weight")
   private float weight = 0.0F;
   @SerializedName("builtin_attachments")
   private Map<AttachmentType, Identifier> builtInAttachments = Maps.newHashMap();
   @SerializedName("script")
   private Identifier script = null;
   @SerializedName("script_param")
   private Map<String, Object> scriptParam = null;
   @SerializedName("charging")
   private EnumMap<FireMode, ChargeData> chargeData = null;

   public ChargeData getChargeData(FireMode fireMode) {
      return this.chargeData != null ? this.chargeData.get(fireMode) : null;
   }

   public Identifier getAmmoId() {
      return this.ammoId;
   }

   public int getAmmoAmount() {
      return this.ammoAmount;
   }

   public int @Nullable [] getExtendedMagAmmoAmount() {
      return this.extendedMagAmmoAmount;
   }

   public boolean isCanCrawl() {
      return this.canCrawl;
   }

   public boolean canSlide() {
      return this.canSlide;
   }

   public Bolt getBolt() {
      return this.bolt;
   }

   @Internal
   public int getRoundsPerMinute() {
      return this.roundsPerMinute;
   }

   public int getRoundsPerMinute(FireMode fireMode) {
      int rpm = this.roundsPerMinute;
      GunFireModeAdjustData fireModeAdjustData = this.getFireModeAdjustData(fireMode);
      if (fireModeAdjustData != null) {
         rpm += fireModeAdjustData.getRoundsPerMinute();
      }

      return rpm <= 0 ? 300 : rpm;
   }

   public BulletData getBulletData() {
      return this.bulletData;
   }

   public float getDrawTime() {
      return this.drawTime;
   }

   public float getPutAwayTime() {
      return this.putAwayTime;
   }

   public float getAimTime() {
      return this.aimTime;
   }

   public float getSprintTime() {
      return this.sprintTime;
   }

   public float getBoltActionTime() {
      return this.boltActionTime;
   }

   public float getBoltFeedTime() {
      return this.boltFeedTime;
   }

   public FireSound getFireSound() {
      return this.fireSound;
   }

   public GunReloadData getReloadData() {
      return this.reloadData;
   }

   public List<FireMode> getFireModeSet() {
      return this.fireModeSet;
   }

   public BurstData getBurstData() {
      return this.burstData;
   }

   public float getWeight() {
      return this.weight;
   }

   @Nullable
   public GunFireModeAdjustData getFireModeAdjustData(FireMode fireMode) {
      return this.fireModeAdjust != null && this.fireModeAdjust.containsKey(fireMode) ? this.fireModeAdjust.get(fireMode) : null;
   }

   public float getCrawlRecoilMultiplier() {
      return this.crawlRecoilMultiplier;
   }

   public GunRecoil getRecoil() {
      return this.recoil;
   }

   public float getHurtBobTweakMultiplier() {
      return this.hurtBobTweakMultiplier;
   }

   public Map<InaccuracyType, Float> getInaccuracy() {
      return this.inaccuracy;
   }

   public void setInaccuracy(Map<InaccuracyType, Float> inaccuracy) {
      this.inaccuracy = inaccuracy;
   }

   public float getInaccuracy(InaccuracyType type) {
      return Math.max(this.inaccuracy.get(type), 0.0F);
   }

   public float getInaccuracy(InaccuracyType type, float addend) {
      return Math.max(this.inaccuracy.get(type) + addend, 0.0F);
   }

   public MoveSpeed getMoveSpeed() {
      return this.moveSpeed;
   }

   public GunMeleeData getMeleeData() {
      return this.gunMeleeData;
   }

   @Nullable
   public GunHeatData getHeatData() {
      return this.gunHeatData;
   }

   public boolean hasHeatData() {
      return this.getHeatData() != null;
   }

   @Nullable
   public List<AttachmentType> getAllowAttachments() {
      return this.allowAttachments;
   }

   public Map<AttachmentType, Identifier> getBuiltInAttachments() {
      return this.builtInAttachments;
   }

   public Map<Identifier, AttachmentData> getExclusiveAttachments() {
      return this.exclusiveAttachments;
   }

   @Nullable
   public Identifier getScript() {
      return this.script;
   }

   @Nullable
   public Map<String, Object> getScriptParam() {
      return this.scriptParam;
   }

   public long getShootInterval(LivingEntity shooter, FireMode fireMode, ItemStack gunStack) {
      int rpm = this.getRoundsPerMinute(fireMode);
      AttachmentCacheProperty cacheProperty = IGunOperator.fromLivingEntity(shooter).getCacheProperty();
      if (cacheProperty != null) {
         rpm = Mth.clamp(cacheProperty.<Integer>getCache(RpmModifier.ID), 1, 1200);
      }

      IGun iGun = IGun.getIGunOrNull(gunStack);
      if (this.hasHeatData()) {
         rpm = (int)(rpm * iGun.lerpRPM(gunStack));
      }

      return 60000L / rpm;
   }

   public long getBurstShootInterval() {
      return this.burstData != null && this.burstData.getBpm() > 0 ? 60000L / this.burstData.getBpm() : 300L;
   }
}
