package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.pojo.display.IDisplay;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class GunDisplay implements IDisplay {
   @SerializedName("model_type")
   private String modelType = "default";
   @SerializedName("model")
   private Identifier modelLocation;
   @SerializedName("texture")
   private Identifier modelTexture;
   @SerializedName("iron_zoom")
   private float ironZoom = 1.2F;
   @SerializedName("zoom_model_fov")
   private float zoomModelFov = 70.0F;
   @Nullable
   @SerializedName("lod")
   private GunLod gunLod;
   @Nullable
   @SerializedName("hud")
   private Identifier hudTextureLocation;
   @Nullable
   @SerializedName("hud_empty")
   private Identifier hudEmptyTextureLocation;
   @Nullable
   @SerializedName("slot")
   private Identifier slotTextureLocation;
   @SerializedName("ammo_count_style")
   @NotNull
   private AmmoCountStyle ammoCountStyle = AmmoCountStyle.NORMAL;
   @SerializedName("damage_style")
   @NotNull
   private DamageStyle damageStyle = DamageStyle.PER_PROJECTILE;
   @Nullable
   @SerializedName("third_person_animation")
   private String thirdPersonAnimation;
   @Nullable
   @SerializedName("animation")
   private Identifier animationLocation;
   @Nullable
   @SerializedName("state_machine")
   private Identifier stateMachineLocation;
   @Nullable
   @SerializedName("state_machine_param")
   private Map<String, Object> stateMachineParam = null;
   @Nullable
   @SerializedName("use_default_animation")
   private DefaultAnimationType defaultAnimationType;
   @Nullable
   @SerializedName("default_animation")
   private Identifier defaultAnimation = null;
   @Nullable
   @SerializedName("player_animator_3rd")
   private Identifier playerAnimator3rd;
   @Nullable
   @SerializedName("3rd_fixed_hand")
   private boolean playerAnimator3rdFixedHand = false;
   @Nullable
   @SerializedName("sounds")
   private Map<String, Identifier> sounds;
   @Nullable
   @SerializedName("preload_sounds")
   private List<String> preloadSounds;
   @Nullable
   @SerializedName("transform")
   private GunTransform transform;
   @Nullable
   @SerializedName("shell")
   private ShellEjection shellEjection;
   @Nullable
   @SerializedName("ammo")
   private GunAmmo gunAmmo;
   @Nullable
   @SerializedName("muzzle_flash")
   private MuzzleFlash muzzleFlash;
   @SerializedName("offhand_show")
   private LayerGunShow offhandShow = new LayerGunShow();
   @Nullable
   @SerializedName("hotbar_show")
   private Map<String, LayerGunShow> hotbarShow = null;
   @SerializedName("text_show")
   private Map<String, TextShow> textShows = Maps.newHashMap();
   @SerializedName("show_crosshair")
   private boolean showCrosshair = false;
   @SerializedName("controllable")
   private EnumMap<FireMode, ControllableData> controllableData = Maps.newEnumMap(FireMode.class);
   @SerializedName("laser")
   private LaserConfig laserConfig;
   @SerializedName("enable_transparency")
   private boolean enableTransparency;

   public String getModelType() {
      return this.modelType;
   }

   public Identifier getModelLocation() {
      return this.modelLocation;
   }

   public Identifier getModelTexture() {
      return this.modelTexture;
   }

   @Nullable
   public GunLod getGunLod() {
      return this.gunLod;
   }

   @Nullable
   public Identifier getHudTextureLocation() {
      return this.hudTextureLocation;
   }

   @Nullable
   public Identifier getHudEmptyTextureLocation() {
      return this.hudEmptyTextureLocation;
   }

   @Nullable
   public Identifier getSlotTextureLocation() {
      return this.slotTextureLocation;
   }

   @Nullable
   public Identifier getAnimationLocation() {
      return this.animationLocation;
   }

   @Nullable
   public Identifier getStateMachineLocation() {
      return this.stateMachineLocation;
   }

   @Nullable
   public Map<String, Object> getStateMachineParam() {
      return this.stateMachineParam;
   }

   @Nullable
   public DefaultAnimationType getDefaultAnimationType() {
      return this.defaultAnimationType;
   }

   @Nullable
   public Identifier getDefaultAnimation() {
      return this.defaultAnimation;
   }

   @Nullable
   public Identifier getPlayerAnimator3rd() {
      return this.playerAnimator3rd;
   }

   @Nullable
   public String getThirdPersonAnimation() {
      return this.thirdPersonAnimation;
   }

   @Nullable
   public Map<String, Identifier> getSounds() {
      return this.sounds;
   }

   @Nullable
   public List<String> getPreloadSounds() {
      return this.preloadSounds;
   }

   @Nullable
   public GunTransform getTransform() {
      return this.transform;
   }

   @Nullable
   public ShellEjection getShellEjection() {
      return this.shellEjection;
   }

   @Nullable
   public GunAmmo getGunAmmo() {
      return this.gunAmmo;
   }

   @Nullable
   public MuzzleFlash getMuzzleFlash() {
      return this.muzzleFlash;
   }

   public LayerGunShow getOffhandShow() {
      return this.offhandShow;
   }

   @Nullable
   public Map<String, LayerGunShow> getHotbarShow() {
      return this.hotbarShow;
   }

   public float getIronZoom() {
      return this.ironZoom;
   }

   public float getZoomModelFov() {
      return this.zoomModelFov;
   }

   public Map<String, TextShow> getTextShows() {
      return this.textShows;
   }

   public boolean isShowCrosshair() {
      return this.showCrosshair;
   }

   public EnumMap<FireMode, ControllableData> getControllableData() {
      return this.controllableData;
   }

   @NotNull
   public AmmoCountStyle getAmmoCountStyle() {
      return this.ammoCountStyle;
   }

   @NotNull
   public DamageStyle getDamageStyle() {
      return this.damageStyle;
   }

   @Nullable
   public LaserConfig getLaserConfig() {
      return this.laserConfig;
   }

   public boolean enablesTransparency() {
      return this.enableTransparency;
   }

   public boolean is3rdFixedHand() {
      return this.playerAnimator3rdFixedHand;
   }

   @Override
   public void init() {
      if (this.modelTexture != null) {
         this.modelTexture = converter.idToFile(this.modelTexture);
      }

      if (this.hudTextureLocation != null) {
         this.hudTextureLocation = converter.idToFile(this.hudTextureLocation);
      }

      if (this.hudEmptyTextureLocation != null) {
         this.hudEmptyTextureLocation = converter.idToFile(this.hudEmptyTextureLocation);
      }

      if (this.slotTextureLocation != null) {
         this.slotTextureLocation = converter.idToFile(this.slotTextureLocation);
      }

      if (this.gunLod != null && this.gunLod.modelTexture != null) {
         this.gunLod.modelTexture = converter.idToFile(this.gunLod.modelTexture);
      }

      if (this.muzzleFlash != null && this.muzzleFlash.texture != null) {
         this.muzzleFlash.texture = converter.idToFile(this.muzzleFlash.texture);
      }
   }
}
