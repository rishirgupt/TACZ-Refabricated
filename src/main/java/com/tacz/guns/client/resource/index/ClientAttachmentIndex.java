package com.tacz.guns.client.resource.index;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.resource.ClientAssetLoadDispatcher;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import com.tacz.guns.client.resource.pojo.display.attachment.AttachmentDisplay;
import com.tacz.guns.client.resource.pojo.display.attachment.AttachmentLod;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.config.client.ResourceConfig;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.AttachmentIndexPOJO;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.util.ColorHex;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class ClientAttachmentIndex {
   private final Object modelLoadLock = new Object();
   private final Map<Identifier, ClientAttachmentSkinIndex> skinIndexMap = Maps.newHashMap();
   private String name;
   private AttachmentDisplay display;
   @Nullable
   private BedrockAttachmentModel attachmentModel;
   @Nullable
   private Identifier modelTexture;
   @Nullable
   private Pair<BedrockAttachmentModel, Identifier> lodModel;
   private Identifier slotTexture;
   private AttachmentData data;
   private float[] viewsFov;
   private float @Nullable [] zoom;
   private int[] views;
   private boolean isScope;
   private boolean isSight;
   private boolean showMuzzle;
   private boolean showMount;
   @Nullable
   private String adapterNodeName;
   @Nullable
   private String tooltipKey;
   private Map<String, Identifier> sounds;
   @Nullable
   private LaserConfig laserConfig;
   private volatile boolean modelsLoaded = false;
   private volatile boolean modelsLoadFailed = false;
   private volatile CompletableFuture<Void> warmUpTask = null;

   private ClientAttachmentIndex() {
   }

   public static ClientAttachmentIndex getInstance(Identifier registryName, AttachmentIndexPOJO indexPOJO) throws IllegalArgumentException {
      ClientAttachmentIndex index = new ClientAttachmentIndex();
      checkIndex(indexPOJO, index);
      AttachmentDisplay display = checkDisplay(indexPOJO, index);
      checkData(indexPOJO, index);
      checkName(indexPOJO, index);
      checkSlotTexture(display, index);
      checkSounds(display, index);
      if (!(Boolean)ResourceConfig.ENABLE_LAZY_CLIENT_ASSET_LOAD.get()) {
         index.ensureModelsLoaded();
      }

      return index;
   }

   private static void checkIndex(AttachmentIndexPOJO attachmentIndexPOJO, ClientAttachmentIndex index) {
      Preconditions.checkArgument(attachmentIndexPOJO != null, "index object file is empty");
      index.tooltipKey = attachmentIndexPOJO.getTooltip();
   }

   @Nonnull
   private static AttachmentDisplay checkDisplay(AttachmentIndexPOJO indexPOJO, ClientAttachmentIndex index) {
      Identifier pojoDisplay = indexPOJO.getDisplay();
      Preconditions.checkArgument(pojoDisplay != null, "index object missing display field");
      AttachmentDisplay display = ClientAssetsManager.INSTANCE.getAttachmentDisplay(pojoDisplay);
      Preconditions.checkArgument(display != null, "there is no corresponding display file");
      index.display = display;
      index.viewsFov = display.getViewsFov();
      if (index.viewsFov == null) {
         Preconditions.checkArgument(display.getFov() > 0.0F, "fov must > 0");
         index.viewsFov = new float[]{display.getFov()};
      } else {
         for (float fov : index.viewsFov) {
            Preconditions.checkArgument(fov > 0.0F, "fov must > 0");
         }
      }

      index.zoom = display.getZoom();
      if (index.zoom != null) {
         for (int i = 0; i < index.zoom.length; i++) {
            if (index.zoom[i] < 1.0F) {
               throw new IllegalArgumentException("zoom must >= 1");
            }
         }
      }

      index.views = display.getViews();
      if (index.views == null) {
         index.views = new int[]{1};
      } else {
         for (int i = 0; i < index.views.length; i++) {
            if (index.views[i] < 1) {
               throw new IllegalArgumentException("view index must >= 1");
            }
         }
      }

      index.isScope = display.isScope();
      index.isSight = display.isSight();
      index.adapterNodeName = display.getAdapterNodeName();
      index.showMuzzle = display.isShowMuzzle();
      index.showMount = display.isShowMount();
      index.laserConfig = display.getLaserConfig();
      return display;
   }

   private static void checkTextShow(AttachmentDisplay display, BedrockAttachmentModel model) {
      if (model != null) {
         Map<String, TextShow> textShowMap = Maps.newHashMap();
         display.getTextShows().forEach((key, textShow) -> {
            if (StringUtils.isNoneBlank(new CharSequence[]{key})) {
               int color = ColorHex.colorTextToRbgInt(textShow.getColorText());
               textShow.setColorInt(color);
               textShowMap.put(key, textShow);
            }
         });
         model.setTextShowList(textShowMap);
      }
   }

   private static void checkData(AttachmentIndexPOJO indexPOJO, ClientAttachmentIndex index) {
      Identifier dataId = indexPOJO.getData();
      Preconditions.checkArgument(dataId != null, "index object missing pojoData field");
      AttachmentData data = CommonAssetsManager.get().getAttachmentData(dataId);
      Preconditions.checkArgument(data != null, "there is no corresponding data file");
      index.data = data;
   }

   private static void checkName(AttachmentIndexPOJO indexPOJO, ClientAttachmentIndex index) {
      index.name = indexPOJO.getName();
      if (StringUtils.isBlank(index.name)) {
         index.name = "custom.tacz.error.no_name";
      }
   }

   private static void checkSlotTexture(AttachmentDisplay display, ClientAttachmentIndex index) {
      index.slotTexture = Objects.requireNonNullElseGet(display.getSlotTextureLocation(), MissingTextureAtlasSprite::getLocation);
   }

   private static void checkTextureAndModel(AttachmentDisplay display, ClientAttachmentIndex index) {
      index.attachmentModel = getOrLoadAttachmentModel(display.getModel());
      if (index.attachmentModel != null) {
         index.attachmentModel.setIsScope(display.isScope());
         index.attachmentModel.setIsSight(display.isSight());
      }

      index.modelTexture = display.getTexture();
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
               checkTextShow(this.display, this.attachmentModel);
               checkLod(this.display, this);
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
         GunMod.LOGGER.warn("Failed to load attachment models {}", this.display.getModel(), throwable);
      }
   }

   @Nullable
   public static BedrockAttachmentModel getOrLoadAttachmentModel(@Nullable Identifier modelLocation) {
      if (modelLocation == null) {
         return null;
      }

      BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(modelLocation);
      return modelPOJO == null ? null : getAttachmentModel(modelPOJO);
   }

   @Nullable
   public static BedrockAttachmentModel getAttachmentModel(BedrockModelPOJO modelPOJO) {
      BedrockAttachmentModel attachmentModel = null;
      if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
         attachmentModel = new BedrockAttachmentModel(modelPOJO, BedrockVersion.LEGACY);
      }

      if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
         attachmentModel = new BedrockAttachmentModel(modelPOJO, BedrockVersion.NEW);
      }

      return attachmentModel;
   }

   private static void checkLod(AttachmentDisplay display, ClientAttachmentIndex index) {
      AttachmentLod gunLod = display.getAttachmentLod();
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
            BedrockAttachmentModel model = new BedrockAttachmentModel(modelPOJO, BedrockVersion.LEGACY);
            index.lodModel = Pair.of(model, texture);
         }

         if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            BedrockAttachmentModel model = new BedrockAttachmentModel(modelPOJO, BedrockVersion.NEW);
            index.lodModel = Pair.of(model, texture);
         }
      }
   }

   private static void checkSounds(AttachmentDisplay display, ClientAttachmentIndex index) {
      Map<String, Identifier> displaySounds = display.getSounds();
      if (displaySounds == null) {
         index.sounds = Maps.newHashMap();
      } else {
         index.sounds = displaySounds;
      }
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getTooltipKey() {
      return this.tooltipKey;
   }

   @Nullable
   public BedrockAttachmentModel getAttachmentModel() {
      this.ensureModelsLoaded();
      return this.attachmentModel;
   }

   @Nullable
   public Identifier getModelTexture() {
      this.ensureModelsLoaded();
      return this.modelTexture;
   }

   @Nullable
   public Pair<BedrockAttachmentModel, Identifier> getLodModel() {
      this.ensureModelsLoaded();
      return this.lodModel;
   }

   public Identifier getSlotTexture() {
      return this.slotTexture;
   }

   public float[] getViewsFov() {
      return this.viewsFov;
   }

   public float @Nullable [] getZoom() {
      return this.zoom;
   }

   public int[] getViews() {
      return this.views;
   }

   public AttachmentData getData() {
      return this.data;
   }

   @Deprecated
   @Nullable
   public ClientAttachmentSkinIndex getSkinIndex(@Nullable Identifier skinName) {
      return null;
   }

   public boolean isScope() {
      return this.isScope;
   }

   public boolean isSight() {
      return this.isSight;
   }

   @Nullable
   public String getAdapterNodeName() {
      return this.adapterNodeName;
   }

   public boolean isShowMuzzle() {
      return this.showMuzzle;
   }

   public boolean isShowMount() {
      return this.showMount;
   }

   public Map<String, Identifier> getSounds() {
      return this.sounds;
   }

   @Nullable
   public LaserConfig getLaserConfig() {
      return this.laserConfig;
   }
}
