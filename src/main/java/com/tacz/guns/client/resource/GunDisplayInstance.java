package com.tacz.guns.client.resource;

import cn.sh1rocu.tacz.TaCZFabric;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.gltf.AnimationStructure;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import com.tacz.guns.api.client.other.GunModelTypeManager;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.pojo.animation.bedrock.BedrockAnimationFile;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import com.tacz.guns.client.resource.pojo.display.ammo.AmmoParticle;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import com.tacz.guns.client.resource.pojo.display.gun.ControllableData;
import com.tacz.guns.client.resource.pojo.display.gun.DamageStyle;
import com.tacz.guns.client.resource.pojo.display.gun.DefaultAnimationType;
import com.tacz.guns.client.resource.pojo.display.gun.GunAmmo;
import com.tacz.guns.client.resource.pojo.display.gun.GunDisplay;
import com.tacz.guns.client.resource.pojo.display.gun.GunLod;
import com.tacz.guns.client.resource.pojo.display.gun.GunTransform;
import com.tacz.guns.client.resource.pojo.display.gun.LayerGunShow;
import com.tacz.guns.client.resource.pojo.display.gun.MuzzleFlash;
import com.tacz.guns.client.resource.pojo.display.gun.ShellEjection;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.client.sound.GunSoundPreload;
import com.tacz.guns.config.client.ResourceConfig;
import com.tacz.guns.sound.SoundManager;
import com.tacz.guns.util.ColorHex;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

@Environment(EnvType.CLIENT)
public class GunDisplayInstance {
   private final Identifier displayId;
   private final GunDisplay display;
   private final Object loadLock = new Object();
   private volatile boolean modelLoaded = false;
   private volatile boolean lodLoaded = false;
   private volatile boolean animationLoaded = false;
   private volatile boolean modelLoadFailed = false;
   private volatile boolean lodLoadFailed = false;
   private volatile boolean animationLoadFailed = false;
   private volatile CompletableFuture<Void> modelWarmUpTask = null;
   private volatile CompletableFuture<Void> lodWarmUpTask = null;
   private volatile CompletableFuture<Void> animationWarmUpTask = null;
   private String thirdPersonAnimation = "empty";
   private BedrockGunModel gunModel;
   @Nullable
   private Pair<BedrockGunModel, Identifier> lodModel;
   private LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine;
   @Nullable
   private LuaTable stateMachineParam;
   @Nullable
   private Identifier playerAnimator3rd = Identifier.fromNamespaceAndPath("tacz", "rifle_default.player_animation");
   private boolean is3rdFixedHand = false;
   private Map<String, Identifier> sounds = Maps.newHashMap();
   private List<Identifier> preloadSounds = Lists.newArrayList();
   private GunTransform transform = GunTransform.getDefault();
   private Identifier modelTexture;
   private Identifier slotTexture = MissingTextureAtlasSprite.getLocation();
   private Identifier hudTexture = MissingTextureAtlasSprite.getLocation();
   @Nullable
   private Identifier hudEmptyTexture;
   @Nullable
   private ShellEjection shellEjection;
   @Nullable
   private MuzzleFlash muzzleFlash;
   private LayerGunShow offhandShow = new LayerGunShow();
   @Nullable
   private Int2ObjectArrayMap<LayerGunShow> hotbarShow;
   private float ironZoom;
   private float zoomModelFov;
   private boolean showCrosshair = false;
   @Nullable
   private AmmoParticle particle;
   private float @Nullable [] tracerColor = null;
   private EnumMap<FireMode, ControllableData> controllableData = new EnumMap<>(FireMode.class);
   private AmmoCountStyle ammoCountStyle = AmmoCountStyle.NORMAL;
   private DamageStyle damageStyle = DamageStyle.PER_PROJECTILE;
   @Nullable
   private LaserConfig laserConfig;
   private boolean enableTransparency;

   GunDisplayInstance(Identifier displayId, GunDisplay display) {
      this.displayId = displayId;
      this.display = Objects.requireNonNull(display, "display");
      this.initBase(display);
      if (!(Boolean)ResourceConfig.ENABLE_LAZY_CLIENT_ASSET_LOAD.get()) {
         this.ensureAnimationLoaded();
      }
   }

   public static GunDisplayInstance create(Identifier displayId, GunDisplay display) throws IllegalArgumentException {
      return new GunDisplayInstance(displayId, display);
   }

   public void warmUpModel() {
      if (!(Boolean)ResourceConfig.ENABLE_LAZY_CLIENT_ASSET_LOAD.get()) {
         this.ensureModelLoaded();
      } else if (!this.modelLoaded && this.modelWarmUpTask == null) {
         synchronized (this.loadLock) {
            if (!this.modelLoaded && this.modelWarmUpTask == null) {
               this.scheduleModelWarmUpLocked();
            }
         }
      }
   }

   public void warmUpLod() {
      if (!(Boolean)ResourceConfig.ENABLE_LAZY_CLIENT_ASSET_LOAD.get()) {
         this.ensureLodLoaded();
      } else if (!this.lodLoaded && this.lodWarmUpTask == null) {
         synchronized (this.loadLock) {
            if (!this.lodLoaded && this.lodWarmUpTask == null) {
               this.lodWarmUpTask = CompletableFuture.runAsync(this::loadLodIfNecessary, ClientAssetLoadDispatcher.executor());
               this.lodWarmUpTask.whenComplete((unused, throwable) -> {
                  if (throwable != null) {
                     this.handleLodLoadFailure(throwable);
                  }
               });
            }
         }
      }
   }

   public void warmUpRuntime() {
      if (!(Boolean)ResourceConfig.ENABLE_LAZY_CLIENT_ASSET_LOAD.get()) {
         this.ensureAnimationLoaded();
      } else if (!this.animationLoaded && this.animationWarmUpTask == null) {
         synchronized (this.loadLock) {
            if (!this.animationLoaded && this.animationWarmUpTask == null) {
               this.scheduleAnimationWarmUpLocked();
            }
         }
      }
   }

   private void ensureModelLoaded() {
      if (!this.modelLoaded && !this.modelLoadFailed) {
         CompletableFuture<Void> task = this.modelWarmUpTask;
         if (task == null) {
            try {
               this.loadModelIfNecessary();
            } catch (Throwable throwable) {
               this.handleModelLoadFailure(throwable);
            }
         } else {
            try {
               task.join();
            } catch (CompletionException exception) {
               this.handleModelLoadFailure(exception.getCause() == null ? exception : exception.getCause());
            }
         }
      }
   }

   private void ensureLodLoaded() {
      if (!this.lodLoaded && !this.lodLoadFailed) {
         CompletableFuture<Void> task = this.lodWarmUpTask;
         if (task == null) {
            try {
               this.loadLodIfNecessary();
            } catch (Throwable throwable) {
               this.handleLodLoadFailure(throwable);
            }
         } else {
            try {
               task.join();
            } catch (CompletionException exception) {
               this.handleLodLoadFailure(exception.getCause() == null ? exception : exception.getCause());
            }
         }
      }
   }

   private void ensureAnimationLoaded() {
      if (!this.animationLoaded && !this.animationLoadFailed) {
         this.ensureModelLoaded();
         if (!this.modelLoaded) {
            this.animationLoadFailed = true;
         } else {
            CompletableFuture<Void> task = this.animationWarmUpTask;
            if (task == null) {
               try {
                  this.loadAnimationAfterModelReady();
               } catch (Throwable throwable) {
                  this.handleAnimationLoadFailure(throwable);
               }
            } else {
               try {
                  task.join();
               } catch (CompletionException exception) {
                  this.handleAnimationLoadFailure(exception.getCause() == null ? exception : exception.getCause());
               }
            }
         }
      }
   }

   private void loadModelIfNecessary() {
      if (!this.modelLoaded) {
         synchronized (this.loadLock) {
            if (!this.modelLoaded) {
               this.checkTextureAndModel(this.display);
               this.checkTextShow(this.display);
               this.modelLoaded = true;
            }
         }
      }
   }

   private void loadLodIfNecessary() {
      if (!this.lodLoaded) {
         synchronized (this.loadLock) {
            if (!this.lodLoaded) {
               this.checkLod(this.display);
               this.lodLoaded = true;
            }
         }
      }
   }

   private CompletableFuture<Void> scheduleModelWarmUpLocked() {
      if (this.modelLoaded) {
         return CompletableFuture.completedFuture(null);
      }

      if (this.modelWarmUpTask == null) {
         this.modelWarmUpTask = CompletableFuture.runAsync(this::loadModelIfNecessary, ClientAssetLoadDispatcher.executor());
         this.modelWarmUpTask.whenComplete((unused, throwable) -> {
            if (throwable != null) {
               this.handleModelLoadFailure(throwable);
            }
         });
      }

      return this.modelWarmUpTask;
   }

   private CompletableFuture<Void> scheduleAnimationWarmUpLocked() {
      if (this.animationLoaded) {
         return CompletableFuture.completedFuture(null);
      }

      if (this.animationWarmUpTask == null) {
         CompletableFuture<Void> modelTask = this.scheduleModelWarmUpLocked();
         this.animationWarmUpTask = modelTask.thenRunAsync(this::loadAnimationAfterModelReady, ClientAssetLoadDispatcher.executor());
         this.animationWarmUpTask.whenComplete((unused, throwable) -> {
            if (throwable != null) {
               this.handleAnimationLoadFailure(throwable);
            }
         });
      }

      return this.animationWarmUpTask;
   }

   private void loadAnimationAfterModelReady() {
      if (!this.animationLoaded) {
         synchronized (this.loadLock) {
            if (!this.animationLoaded) {
               if (!this.modelLoaded) {
                  throw new IllegalStateException("Gun animation requires model to be loaded first");
               }

               this.checkAnimation(this.display);
               this.animationLoaded = true;
            }
         }
      }
   }

   private void handleModelLoadFailure(Throwable throwable) {
      boolean shouldLog = false;
      synchronized (this.loadLock) {
         if (!this.modelLoaded) {
            shouldLog = !this.modelLoadFailed;
            this.modelWarmUpTask = null;
            this.modelLoadFailed = true;
         }
      }

      if (shouldLog) {
         GunMod.LOGGER.warn("Failed to load gun model {}", this.displayId, throwable);
      }
   }

   private void handleLodLoadFailure(Throwable throwable) {
      boolean shouldLog = false;
      synchronized (this.loadLock) {
         if (!this.lodLoaded) {
            shouldLog = !this.lodLoadFailed;
            this.lodWarmUpTask = null;
            this.lodLoadFailed = true;
         }
      }

      if (shouldLog) {
         GunMod.LOGGER.warn("Failed to load gun lod {}", this.displayId, throwable);
      }
   }

   private void handleAnimationLoadFailure(Throwable throwable) {
      boolean shouldLog = false;
      synchronized (this.loadLock) {
         if (!this.animationLoaded) {
            shouldLog = !this.animationLoadFailed;
            this.animationWarmUpTask = null;
            this.animationLoadFailed = true;
         }
      }

      if (shouldLog) {
         GunMod.LOGGER.warn("Failed to load gun animation runtime {}", this.displayId, throwable);
      }
   }

   private void initBase(GunDisplay display) {
      this.checkSlotTexture(display);
      this.checkHUDTexture(display);
      this.checkSounds(display);
      this.checkTransform(display);
      this.checkShellEjection(display);
      this.checkGunAmmo(display);
      this.checkMuzzleFlash(display);
      this.checkLayerGunShow(display);
      this.checkIronZoom(display);
      this.checkZoomModelFov(display);
      this.thirdPersonAnimation = (String)StringUtils.defaultIfBlank(display.getThirdPersonAnimation(), "empty");
      if (display.getPlayerAnimator3rd() != null) {
         this.playerAnimator3rd = display.getPlayerAnimator3rd();
         this.is3rdFixedHand = display.is3rdFixedHand();
      }

      this.showCrosshair = display.isShowCrosshair();
      this.controllableData = display.getControllableData();
      this.ammoCountStyle = display.getAmmoCountStyle();
      this.damageStyle = display.getDamageStyle();
      this.laserConfig = display.getLaserConfig();
      this.enableTransparency = display.enablesTransparency();
   }

   private void checkIronZoom(GunDisplay display) {
      this.ironZoom = display.getIronZoom();
      if (this.ironZoom < 1.0F) {
         this.ironZoom = 1.0F;
      }
   }

   private void checkZoomModelFov(GunDisplay display) {
      this.zoomModelFov = display.getZoomModelFov();
      if (this.zoomModelFov > 70.0F) {
         this.zoomModelFov = 70.0F;
      }
   }

   private void checkTextShow(GunDisplay display) {
      Map<String, TextShow> textShowMap = Maps.newHashMap();
      display.getTextShows().forEach((key, value) -> {
         if (StringUtils.isNoneBlank(new CharSequence[]{key})) {
            int color = ColorHex.colorTextToRbgInt(value.getColorText());
            value.setColorInt(color);
            textShowMap.put(key, value);
         }
      });
      this.gunModel.setTextShowList(textShowMap);
   }

   private void checkTextureAndModel(GunDisplay display) {
      String modelType = display.getModelType();
      BiFunction<BedrockModelPOJO, BedrockVersion, ? extends BedrockGunModel> constructor = GunModelTypeManager.getModelInstanceConstructor(modelType);
      Identifier modelLocation = display.getModelLocation();
      Preconditions.checkArgument(modelLocation != null, "display object missing model field");
      BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(modelLocation);
      Preconditions.checkArgument(modelPOJO != null, "there is no corresponding model file");
      Identifier textureLocation = display.getModelTexture();
      Preconditions.checkArgument(textureLocation != null, "missing default texture");
      this.modelTexture = textureLocation;
      if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
         this.gunModel = constructor.apply(modelPOJO, BedrockVersion.LEGACY);
      }

      if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
         this.gunModel = constructor.apply(modelPOJO, BedrockVersion.NEW);
      }

      Preconditions.checkArgument(this.gunModel != null, "there is no model data in the model file");
   }

   private void checkLod(GunDisplay display) {
      GunLod gunLod = display.getGunLod();
      if (gunLod != null) {
         Identifier texture = gunLod.getModelTexture();
         if (gunLod.getModelLocation() == null) {
            return;
         }

         if (texture == null) {
            return;
         }

         BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(gunLod.getModelLocation());
         if (modelPOJO == null) {
            return;
         }

         if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
            BedrockGunModel model = new BedrockGunModel(modelPOJO, BedrockVersion.LEGACY);
            this.lodModel = Pair.of(model, texture);
         }

         if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            BedrockGunModel model = new BedrockGunModel(modelPOJO, BedrockVersion.NEW);
            this.lodModel = Pair.of(model, texture);
         }
      }
   }

   private void checkAnimation(GunDisplay display) {
      Identifier location = display.getAnimationLocation();
      AnimationController controller;
      if (location == null) {
         controller = new AnimationController(Lists.newArrayList(), this.gunModel);
      } else {
         AnimationStructure gltfAnimations = ClientAssetsManager.INSTANCE.getGltfAnimation(location);
         BedrockAnimationFile bedrockAnimationFile = ClientAssetsManager.INSTANCE.getBedrockAnimations(location);
         if (bedrockAnimationFile != null) {
            controller = Animations.createControllerFromBedrock(bedrockAnimationFile, this.gunModel);
         } else {
            if (gltfAnimations == null) {
               throw new IllegalArgumentException("animation not found: " + location);
            }

            controller = Animations.createControllerFromGltf(gltfAnimations, this.gunModel);
         }

         Identifier defaultAnimation = display.getDefaultAnimation();
         if (defaultAnimation != null) {
            BedrockAnimationFile animationFile = ClientAssetsManager.INSTANCE.getBedrockAnimations(defaultAnimation);
            if (animationFile == null) {
               throw new IllegalArgumentException("animation not found: " + defaultAnimation);
            }

            for (ObjectAnimation animation : Animations.createAnimationFromBedrock(animationFile)) {
               controller.providePrototypeIfAbsent(animation.name, () -> new ObjectAnimation(animation));
            }
         } else {
            DefaultAnimationType defaultAnimationType = display.getDefaultAnimationType();
            if (defaultAnimationType != null) {
               switch (defaultAnimationType) {
                  case RIFLE:
                     for (ObjectAnimation animation : InternalAssetLoader.getDefaultRifleAnimations()) {
                        controller.providePrototypeIfAbsent(animation.name, () -> new ObjectAnimation(animation));
                     }
                     break;
                  case PISTOL:
                     for (ObjectAnimation animation : InternalAssetLoader.getDefaultPistolAnimations()) {
                        controller.providePrototypeIfAbsent(animation.name, () -> new ObjectAnimation(animation));
                     }
               }
            }
         }
      }

      Identifier stateMachineLocation = display.getStateMachineLocation();
      if (stateMachineLocation == null) {
         stateMachineLocation = Identifier.fromNamespaceAndPath("tacz", "default_state_machine");
      }

      LuaTable script = ClientAssetsManager.INSTANCE.getScript(stateMachineLocation);
      if (script == null) {
         throw new IllegalArgumentException("statemachine not found: " + stateMachineLocation);
      }

      this.animationStateMachine = new LuaStateMachineFactory<GunAnimationStateContext>().setController(controller).setLuaScripts(script).build();
      Map<String, Object> params = display.getStateMachineParam();
      if (params != null) {
         this.stateMachineParam = new LuaTable();

         for (Entry<String, Object> entry : params.entrySet()) {
            this.stateMachineParam.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
         }
      }
   }

   private void checkSounds(GunDisplay display) {
      this.sounds = Maps.newHashMap();
      this.preloadSounds = Lists.newArrayList();
      Map<String, Identifier> soundMaps = display.getSounds();
      if (soundMaps != null && !soundMaps.isEmpty()) {
         soundMaps.putIfAbsent(SoundManager.DRY_FIRE_SOUND, Identifier.fromNamespaceAndPath("tacz", SoundManager.DRY_FIRE_SOUND));
         soundMaps.putIfAbsent(SoundManager.FIRE_SELECT, Identifier.fromNamespaceAndPath("tacz", SoundManager.FIRE_SELECT));
         soundMaps.putIfAbsent(SoundManager.HEAD_HIT_SOUND, Identifier.fromNamespaceAndPath("tacz", SoundManager.HEAD_HIT_SOUND));
         soundMaps.putIfAbsent(SoundManager.FLESH_HIT_SOUND, Identifier.fromNamespaceAndPath("tacz", SoundManager.FLESH_HIT_SOUND));
         soundMaps.putIfAbsent(SoundManager.KILL_SOUND, Identifier.fromNamespaceAndPath("tacz", SoundManager.KILL_SOUND));
         soundMaps.putIfAbsent(SoundManager.MELEE_BAYONET, Identifier.fromNamespaceAndPath("tacz", "melee_bayonet/melee_bayonet_01"));
         soundMaps.putIfAbsent(SoundManager.MELEE_STOCK, Identifier.fromNamespaceAndPath("tacz", "melee_stock/melee_stock_01"));
         soundMaps.putIfAbsent(SoundManager.MELEE_PUSH, Identifier.fromNamespaceAndPath("tacz", "melee_stock/melee_stock_02"));
         this.sounds.putAll(soundMaps);
         Set<Identifier> preloadSet = new LinkedHashSet<>();

         for (String name : GunSoundPreload.DEFAULT_PRELOAD_NAMES) {
            this.addPreloadSound(preloadSet, name);
         }

         List<String> configuredPreloadSounds = display.getPreloadSounds();
         if (configuredPreloadSounds != null) {
            for (String name : configuredPreloadSounds) {
               this.addPreloadSound(preloadSet, name);
            }
         }

         this.preloadSounds.addAll(preloadSet);
      }
   }

   private void addPreloadSound(Set<Identifier> preloadSet, String name) {
      if (!StringUtils.isBlank(name)) {
         Identifier soundId = this.sounds.get(name);
         if (soundId != null) {
            preloadSet.add(soundId);
         }
      }
   }

   private void checkTransform(GunDisplay display) {
      GunTransform readTransform = display.getTransform();
      if (readTransform != null && readTransform.getScale() != null) {
         this.transform = display.getTransform();
      } else {
         this.transform = GunTransform.getDefault();
      }
   }

   private void checkSlotTexture(GunDisplay display) {
      this.slotTexture = Objects.requireNonNullElseGet(display.getSlotTextureLocation(), MissingTextureAtlasSprite::getLocation);
   }

   private void checkHUDTexture(GunDisplay display) {
      this.hudTexture = Objects.requireNonNullElseGet(display.getHudTextureLocation(), MissingTextureAtlasSprite::getLocation);
      this.hudEmptyTexture = display.getHudEmptyTextureLocation();
   }

   private void checkShellEjection(GunDisplay display) {
      this.shellEjection = display.getShellEjection();
   }

   private void checkGunAmmo(GunDisplay display) {
      GunAmmo displayGunAmmo = display.getGunAmmo();
      if (displayGunAmmo != null) {
         String tracerColorText = displayGunAmmo.getTracerColor();
         if (StringUtils.isNoneBlank(new CharSequence[]{tracerColorText})) {
            this.tracerColor = ColorHex.colorTextToRbgFloatArray(tracerColorText);
         }

         AmmoParticle particle = displayGunAmmo.getParticle();
         if (particle != null) {
            try {
               String name = particle.getName();
               if (StringUtils.isNoneBlank(new CharSequence[0])) {
                  particle.setParticleOptions(ParticleArgument.readParticle(new StringReader(name), TaCZFabric.VANILLA_ACCESS));
                  Preconditions.checkArgument(particle.getCount() > 0, "particle count must be greater than 0");
                  Preconditions.checkArgument(particle.getLifeTime() > 0, "particle life time must be greater than 0");
                  this.particle = particle;
               }
            } catch (CommandSyntaxException e) {
               e.fillInStackTrace();
            }
         }
      }
   }

   private void checkMuzzleFlash(GunDisplay display) {
      this.muzzleFlash = display.getMuzzleFlash();
      if (this.muzzleFlash != null && this.muzzleFlash.getTexture() == null) {
         this.muzzleFlash = null;
      }
   }

   private void checkLayerGunShow(GunDisplay display) {
      this.offhandShow = display.getOffhandShow();
      if (this.offhandShow == null) {
         this.offhandShow = new LayerGunShow();
      }

      Map<String, LayerGunShow> show = display.getHotbarShow();
      if (show != null && !show.isEmpty()) {
         this.hotbarShow = new Int2ObjectArrayMap();

         for (String key : show.keySet()) {
            try {
               this.hotbarShow.put(Integer.parseInt(key), show.get(key));
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("index number is error: " + key);
            }
         }
      }
   }

   @Nullable
   public BedrockGunModel getGunModel() {
      this.ensureModelLoaded();
      return this.gunModel;
   }

   @Nullable
   public Pair<BedrockGunModel, Identifier> getLodModel() {
      this.ensureLodLoaded();
      return this.lodModel;
   }

   @Nullable
   public LuaAnimationStateMachine<GunAnimationStateContext> getAnimationStateMachine() {
      this.ensureAnimationLoaded();
      return this.animationStateMachine;
   }

   @Nullable
   public LuaTable getStateMachineParam() {
      this.ensureAnimationLoaded();
      return this.stateMachineParam;
   }

   @Nullable
   public Identifier getSounds(String name) {
      return this.sounds.get(name);
   }

   public List<Identifier> getPreloadSounds() {
      return this.preloadSounds;
   }

   public GunTransform getTransform() {
      return this.transform;
   }

   public Identifier getSlotTexture() {
      return this.slotTexture;
   }

   public Identifier getHUDTexture() {
      return this.hudTexture;
   }

   @Nullable
   public Identifier getHudEmptyTexture() {
      return this.hudEmptyTexture;
   }

   public Identifier getModelTexture() {
      this.ensureModelLoaded();
      return this.modelTexture != null ? this.modelTexture : MissingTextureAtlasSprite.getLocation();
   }

   public String getThirdPersonAnimation() {
      return this.thirdPersonAnimation;
   }

   @Nullable
   public ShellEjection getShellEjection() {
      return this.shellEjection;
   }

   public float @Nullable [] getTracerColor() {
      return this.tracerColor;
   }

   @Nullable
   public AmmoParticle getParticle() {
      return this.particle;
   }

   @Nullable
   public MuzzleFlash getMuzzleFlash() {
      return this.muzzleFlash;
   }

   public LayerGunShow getOffhandShow() {
      return this.offhandShow;
   }

   @Nullable
   public Int2ObjectArrayMap<LayerGunShow> getHotbarShow() {
      return this.hotbarShow;
   }

   public float getIronZoom() {
      return this.ironZoom;
   }

   public float getZoomModelFov() {
      return this.zoomModelFov;
   }

   public boolean isShowCrosshair() {
      return this.showCrosshair;
   }

   @Nullable
   public Identifier getPlayerAnimator3rd() {
      return this.playerAnimator3rd;
   }

   public boolean is3rdFixedHand() {
      return this.is3rdFixedHand;
   }

   public EnumMap<FireMode, ControllableData> getControllableData() {
      return this.controllableData;
   }

   public AmmoCountStyle getAmmoCountStyle() {
      return this.ammoCountStyle;
   }

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
}
