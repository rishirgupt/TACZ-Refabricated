package com.tacz.guns.client.resource.pojo.model;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Type;
import java.util.List;
import javax.annotation.Nullable;

public class CubesItem {
   private List<Float> uv;
   private FaceUVsItem faceUv;
   private boolean mirror = false;
   private boolean hasMirror = false;
   @Expose
   @SerializedName("inflate")
   private float inflate;
   @Expose
   @SerializedName("size")
   private List<Float> size;
   @Expose
   @SerializedName("origin")
   private List<Float> origin;
   @Expose
   @SerializedName("rotation")
   private List<Float> rotation;
   @Expose
   @SerializedName("pivot")
   private List<Float> pivot;

   public List<Float> getUv() {
      return this.uv;
   }

   @Nullable
   public FaceUVsItem getFaceUv() {
      return this.faceUv;
   }

   public boolean isMirror() {
      return this.mirror;
   }

   public void setMirror(boolean mirror) {
      this.mirror = mirror;
   }

   public boolean isHasMirror() {
      return this.hasMirror;
   }

   public float getInflate() {
      return this.inflate;
   }

   public List<Float> getSize() {
      return this.size;
   }

   public List<Float> getOrigin() {
      return this.origin;
   }

   @Nullable
   public List<Float> getRotation() {
      return this.rotation;
   }

   @Nullable
   public List<Float> getPivot() {
      return this.pivot;
   }

   public static class Deserializer implements JsonDeserializer<CubesItem> {
      public CubesItem deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
         CubesItem cube = (CubesItem)new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, CubesItem.class);
         if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            JsonElement uvElement = obj.get("uv");
            if (uvElement.isJsonArray()) {
               cube.uv = Lists.newArrayList();
               JsonArray array = uvElement.getAsJsonArray();

               for (int i = 0; i < array.size(); i++) {
                  cube.uv.add(array.get(i).getAsFloat());
               }
            }

            if (uvElement.isJsonObject()) {
               cube.faceUv = (FaceUVsItem)new Gson().fromJson(uvElement, FaceUVsItem.class);
            }

            JsonElement mirrorElement = obj.get("mirror");
            if (mirrorElement != null && mirrorElement.isJsonPrimitive()) {
               JsonPrimitive primitive = mirrorElement.getAsJsonPrimitive();
               if (primitive.isBoolean()) {
                  cube.mirror = primitive.getAsBoolean();
                  cube.hasMirror = true;
               }
            }
         }

         return cube;
      }
   }
}
