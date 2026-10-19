package com.tacz.guns.client.resource.pojo.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

public class BedrockModelPOJO {
   @SerializedName("format_version")
   private String formatVersion;
   @SerializedName("geometry.model")
   @Nullable
   private GeometryModelLegacy geometryModelLegacy;
   @SerializedName("minecraft:geometry")
   @Nullable
   private List<GeometryModelNew> geometryModelNew;

   public String getFormatVersion() {
      return this.formatVersion;
   }

   @Nullable
   public GeometryModelLegacy getGeometryModelLegacy() {
      return this.geometryModelLegacy;
   }

   @Nullable
   public GeometryModelNew getGeometryModelNew() {
      return this.geometryModelNew == null ? null : this.geometryModelNew.get(0);
   }
}
