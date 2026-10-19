package com.tacz.guns.client.resource.index;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.resource.ClientAssetLoadDispatcher;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.display.ammo.AmmoDisplay;
import com.tacz.guns.client.resource.pojo.display.ammo.AmmoEntityDisplay;
import com.tacz.guns.client.resource.pojo.display.ammo.AmmoParticle;
import com.tacz.guns.client.resource.pojo.display.ammo.AmmoTransform;
import com.tacz.guns.client.resource.pojo.display.ammo.ShellDisplay;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.config.client.ResourceConfig;
import com.tacz.guns.resource.pojo.AmmoIndexPOJO;
import com.tacz.guns.util.ColorHex;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientAmmoIndex {
   private final Object modelLoadLock = new Object();
   private String name;
   private AmmoDisplay display;
   @Nullable
   private BedrockAmmoModel ammoModel;
   @Nullable
   private Identifier modelTextureLocation;
   private Identifier slotTextureLocation;
   @Nullable
   private BedrockAmmoModel ammoEntityModel;
   @Nullable
   private Identifier ammoEntityTextureLocation;
   @Nullable
   private BedrockAmmoModel shellModel;
   @Nullable
   private Identifier shellTextureLocation;
   private int stackSize;
   @Nullable
   private AmmoParticle particle;
   private float[] tracerColor = new float[]{1.0F, 1.0F, 1.0F};
   private AmmoTransform transform;
   @Nullable
   private String tooltipKey;
   private volatile boolean modelsLoaded = false;
   private volatile boolean modelsLoadFailed = false;
   private volatile CompletableFuture<Void> warmUpTask = null;

   private ClientAmmoIndex() {
   }

   public static ClientAmmoIndex getInstance(AmmoIndexPOJO clientPojo) throws IllegalArgumentException {
      ClientAmmoIndex index = new ClientAmmoIndex();
      checkIndex(clientPojo, index);
      AmmoDisplay display = checkDisplay(clientPojo, index);
      checkName(clientPojo, index);
      checkSlotTexture(display, index);
      checkStackSize(clientPojo, index);
      checkParticle(display, index);
      checkTracerColor(display, index);
      checkTransform(display, index);
      if (!(Boolean)ResourceConfig.ENABLE_LAZY_CLIENT_ASSET_LOAD.get()) {
         index.ensureModelsLoaded();
      }

      return index;
   }

   private static void checkIndex(AmmoIndexPOJO ammoIndexPOJO, ClientAmmoIndex index) {
      Preconditions.checkArgument(ammoIndexPOJO != null, "index object file is empty");
      index.tooltipKey = ammoIndexPOJO.getTooltip();
   }

   private static void checkName(AmmoIndexPOJO ammoIndexPOJO, ClientAmmoIndex index) {
      index.name = ammoIndexPOJO.getName();
      if (StringUtils.isBlank(index.name)) {
         index.name = "custom.tacz.error.no_name";
      }
   }

   @NotNull
   private static AmmoDisplay checkDisplay(AmmoIndexPOJO ammoIndexPOJO, ClientAmmoIndex index) {
      Identifier pojoDisplay = ammoIndexPOJO.getDisplay();
      Preconditions.checkArgument(pojoDisplay != null, "index object missing display field");
      AmmoDisplay display = ClientAssetsManager.INSTANCE.getAmmoDisplay(pojoDisplay);
      Preconditions.checkArgument(display != null, "there is no corresponding display file");
      index.display = display;
      return display;
   }

   public void warmUp() {
      if (!(Boolean)ResourceConfig.ENABLE_LAZY_CLIENT_ASSET_LOAD.get()) {
         this.ensureModelsLoaded();
      } else if (!this.modelsLoaded && !this.modelsLoadFailed && this.warmUpTask == null) {
         synchronized (this.modelLoadLock) {
            if (!this.modelsLoaded && !this.modelsLoadFailed && this.warmUpTask == null) {
               this.warmUpTask = CompletableFuture.runAsync(this::loadModelsIfNecessary, ClientAssetLoadDispatcher.executor());
               this.warmUpTask.whenComplete((unused, throwable) -> {
                  if (throwable != null) {
                     this.handleModelsLoadFailure(throwable);
                  }
               });
            }
         }
      }
   }

   private void ensureModelsLoaded() {
      if (!this.modelsLoaded && !this.modelsLoadFailed) {
         CompletableFuture<Void> task = this.warmUpTask;
         if (task == null) {
            try {
               this.loadModelsIfNecessary();
            } catch (Throwable throwable) {
               this.handleModelsLoadFailure(throwable);
            }
         } else {
            try {
               task.join();
            } catch (CompletionException exception) {
               this.handleModelsLoadFailure(exception.getCause() == null ? exception : exception.getCause());
            }
         }
      }
   }

   private void loadModelsIfNecessary() {
      if (!this.modelsLoaded) {
         synchronized (this.modelLoadLock) {
            if (!this.modelsLoaded) {
               checkTextureAndModel(this.display, this);
               checkAmmoEntity(this.display, this);
               checkShell(this.display, this);
               this.modelsLoaded = true;
            }
         }
      }
   }

   private void handleModelsLoadFailure(Throwable throwable) {
      boolean shouldLog = false;
      synchronized (this.modelLoadLock) {
         if (!this.modelsLoaded) {
            shouldLog = !this.modelsLoadFailed;
            this.warmUpTask = null;
            this.modelsLoadFailed = true;
         }
      }

      if (shouldLog) {
         GunMod.LOGGER.warn("Failed to load ammo models {}", this.display.getModelLocation(), throwable);
      }
   }

   private static void checkTextureAndModel(AmmoDisplay display, ClientAmmoIndex index) {
      Identifier modelLocation = display.getModelLocation();
      if (modelLocation != null) {
         BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(modelLocation);
         Preconditions.checkArgument(modelPOJO != null, "there is no corresponding model file");
         index.modelTextureLocation = display.getModelTexture();
         if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
            index.ammoModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.LEGACY);
         }

         if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            index.ammoModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.NEW);
         }
      }
   }

   private static void checkSlotTexture(AmmoDisplay display, ClientAmmoIndex index) {
      index.slotTextureLocation = Objects.requireNonNullElseGet(display.getSlotTextureLocation(), MissingTextureAtlasSprite::getLocation);
   }

   private static void checkAmmoEntity(AmmoDisplay display, ClientAmmoIndex index) {
      AmmoEntityDisplay ammoEntity = display.getAmmoEntity();
      if (ammoEntity != null && ammoEntity.getModelLocation() != null && ammoEntity.getModelTexture() != null) {
         index.ammoEntityTextureLocation = ammoEntity.getModelTexture();
         Identifier modelLocation = ammoEntity.getModelLocation();
         BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(modelLocation);
         if (modelPOJO == null) {
            return;
         }

         if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
            index.ammoEntityModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.LEGACY);
         }

         if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            index.ammoEntityModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.NEW);
         }
      }
   }

   private static void checkShell(AmmoDisplay display, ClientAmmoIndex index) {
      ShellDisplay shellDisplay = display.getShellDisplay();
      if (shellDisplay != null && shellDisplay.getModelLocation() != null && shellDisplay.getModelTexture() != null) {
         index.shellTextureLocation = shellDisplay.getModelTexture();
         Identifier modelLocation = shellDisplay.getModelLocation();
         BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(modelLocation);
         if (modelPOJO == null) {
            return;
         }

         if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
            index.shellModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.LEGACY);
         }

         if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            index.shellModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.NEW);
         }
      }
   }

   private static void checkParticle(AmmoDisplay display, ClientAmmoIndex index) {
      if (display.getParticle() != null) {
         try {
            AmmoParticle particle = display.getParticle();
            String name = particle.getName();
            if (StringUtils.isNoneBlank(new CharSequence[0])) {
               particle.setParticleOptions(ParticleArgument.readParticle(new StringReader(name), Minecraft.getInstance().level.registryAccess()));
               Preconditions.checkArgument(particle.getCount() > 0, "particle count must be greater than 0");
               Preconditions.checkArgument(particle.getLifeTime() > 0, "particle life time must be greater than 0");
               index.particle = particle;
            }
         } catch (CommandSyntaxException e) {
            e.fillInStackTrace();
         }
      }
   }

   private static void checkTracerColor(AmmoDisplay display, ClientAmmoIndex index) {
      String tracerColorText = display.getTracerColor();
      if (StringUtils.isNoneBlank(new CharSequence[]{tracerColorText})) {
         index.tracerColor = ColorHex.colorTextToRbgFloatArray(tracerColorText);
      }
   }

   private static void checkTransform(AmmoDisplay display, ClientAmmoIndex index) {
      AmmoTransform readTransform = display.getTransform();
      if (readTransform != null && readTransform.getScale() != null) {
         index.transform = display.getTransform();
      } else {
         index.transform = AmmoTransform.getDefault();
      }
   }

   private static void checkStackSize(AmmoIndexPOJO clientPojo, ClientAmmoIndex index) {
      index.stackSize = Math.max(clientPojo.getStackSize(), 1);
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getTooltipKey() {
      return this.tooltipKey;
   }

   @Nullable
   public BedrockAmmoModel getAmmoModel() {
      this.ensureModelsLoaded();
      return this.ammoModel;
   }

   @Nullable
   public Identifier getModelTextureLocation() {
      this.ensureModelsLoaded();
      return this.modelTextureLocation;
   }

   public Identifier getSlotTextureLocation() {
      return this.slotTextureLocation;
   }

   public int getStackSize() {
      return this.stackSize;
   }

   @Nullable
   public BedrockAmmoModel getAmmoEntityModel() {
      this.ensureModelsLoaded();
      return this.ammoEntityModel;
   }

   @Nullable
   public Identifier getAmmoEntityTextureLocation() {
      this.ensureModelsLoaded();
      return this.ammoEntityTextureLocation;
   }

   @Nullable
   public BedrockAmmoModel getShellModel() {
      this.ensureModelsLoaded();
      return this.shellModel;
   }

   @Nullable
   public Identifier getShellTextureLocation() {
      this.ensureModelsLoaded();
      return this.shellTextureLocation;
   }

   @Nullable
   public AmmoParticle getParticle() {
      return this.particle;
   }

   public float[] getTracerColor() {
      return this.tracerColor;
   }

   public AmmoTransform getTransform() {
      return this.transform;
   }
}
