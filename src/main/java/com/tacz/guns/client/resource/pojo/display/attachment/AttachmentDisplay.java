package com.tacz.guns.client.resource.pojo.display.attachment;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.client.resource.pojo.display.IDisplay;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class AttachmentDisplay implements IDisplay {
   @SerializedName("laser")
   private LaserConfig laserConfig;
   @SerializedName("slot")
   private Identifier slotTextureLocation;
   @SerializedName("model")
   private Identifier model;
   @SerializedName("texture")
   private Identifier texture;
   @SerializedName("lod")
   @Nullable
   private AttachmentLod attachmentLod;
   @SerializedName("adapter")
   @Nullable
   private String adapterNodeName;
   @SerializedName("show_muzzle")
   private boolean showMuzzle = false;
   @SerializedName("show_mount")
   private boolean showMount = true;
   @SerializedName("text_show")
   private Map<String, TextShow> textShows = Maps.newHashMap();
   @SerializedName("zoom")
   @Nullable
   private float[] zoom;
   @SerializedName("views")
   @Nullable
   private int[] views;
   @SerializedName("scope")
   private boolean isScope = false;
   @SerializedName("sight")
   private boolean isSight = false;
   @SerializedName("fov")
   private float fov = 70.0F;
   @SerializedName("views_fov")
   @Nullable
   private float[] viewsFov;
   @SerializedName("sounds")
   private Map<String, Identifier> sounds = Maps.newHashMap();

   public Identifier getSlotTextureLocation() {
      return this.slotTextureLocation;
   }

   public Identifier getModel() {
      return this.model;
   }

   public Identifier getTexture() {
      return this.texture;
   }

   @Nullable
   public AttachmentLod getAttachmentLod() {
      return this.attachmentLod;
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

   public Map<String, TextShow> getTextShows() {
      return this.textShows;
   }

   @Nullable
   public float[] getZoom() {
      return this.zoom;
   }

   @Nullable
   public int[] getViews() {
      return this.views;
   }

   public boolean isScope() {
      return this.isScope;
   }

   public boolean isSight() {
      return this.isSight;
   }

   public float getFov() {
      return this.fov;
   }

   @Nullable
   public float[] getViewsFov() {
      return this.viewsFov;
   }

   public Map<String, Identifier> getSounds() {
      return this.sounds;
   }

   @Nullable
   public LaserConfig getLaserConfig() {
      return this.laserConfig;
   }

   @Override
   public void init() {
      if (this.slotTextureLocation != null) {
         this.slotTextureLocation = converter.idToFile(this.slotTextureLocation);
      }

      if (this.texture != null) {
         this.texture = converter.idToFile(this.texture);
      }

      if (this.attachmentLod != null && this.attachmentLod.modelTexture != null) {
         this.attachmentLod.modelTexture = converter.idToFile(this.attachmentLod.modelTexture);
      }
   }
}
