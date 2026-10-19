package com.tacz.guns.client.resource.pojo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3f;

public class CommonTransformObject {
   @Nullable
   private Vector3f translation;
   @Nullable
   private Vector3f rotation;
   @Nullable
   private Vector3f scale;

   public CommonTransformObject() {
   }

   public CommonTransformObject(@Nullable Vector3f translation, @Nullable Vector3f rotation, @Nullable Vector3f scale) {
      this.translation = translation;
      this.rotation = rotation;
      this.scale = scale;
   }

   @Nonnull
   public Vector3f getTranslation() {
      if (this.translation == null) {
         this.translation = new Vector3f(0.0F, 0.0F, 0.0F);
      }

      return this.translation;
   }

   @Nonnull
   public Vector3f getRotation() {
      if (this.rotation == null) {
         this.rotation = new Vector3f(0.0F, 0.0F, 0.0F);
      }

      return this.rotation;
   }

   @Nonnull
   public Vector3f getScale() {
      if (this.scale == null) {
         this.scale = new Vector3f(1.0F, 1.0F, 1.0F);
      }

      return this.scale;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof CommonTransformObject object)) {
         return false;
      } else {
         boolean flag1;
         if (this.translation != null && object.translation != null) {
            flag1 = this.translation.equals(object.translation);
         } else {
            flag1 = this.translation == null && object.translation == null;
         }

         boolean flag2;
         if (this.rotation != null && object.rotation != null) {
            flag2 = this.rotation.equals(object.rotation);
         } else {
            flag2 = this.rotation == null && object.rotation == null;
         }

         boolean flag3;
         if (this.scale != null && object.scale != null) {
            flag3 = this.scale.equals(object.scale);
         } else {
            flag3 = this.scale == null && object.scale == null;
         }

         return flag1 && flag2 && flag3;
      }
   }

   public CommonTransformObject lerp(CommonTransformObject target, float alpha) {
      CommonTransformObject object = new CommonTransformObject();
      object.translation = this.translation == null ? new Vector3f(0.0F, 0.0F, 0.0F) : new Vector3f(this.translation);
      object.rotation = this.rotation == null ? new Vector3f(0.0F, 0.0F, 0.0F) : new Vector3f(this.rotation);
      object.scale = this.scale == null ? new Vector3f(1.0F, 1.0F, 1.0F) : new Vector3f(this.scale);
      object.translation.lerp(target.getTranslation(), alpha);
      object.rotation.lerp(target.getRotation(), alpha);
      object.scale.lerp(target.getScale(), alpha);
      return object;
   }

   public static class Serializer implements JsonDeserializer<CommonTransformObject>, JsonSerializer<CommonTransformObject> {
      public CommonTransformObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         if (json.isJsonObject()) {
            CommonTransformObject transformObject = new CommonTransformObject();
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement translationElement = jsonObject.get("translation");
            JsonElement rotationElement = jsonObject.get("rotation");
            JsonElement scaleElement = jsonObject.get("scale");
            if (translationElement != null) {
               transformObject.translation = (Vector3f)context.deserialize(translationElement, Vector3f.class);
            }

            if (rotationElement != null) {
               transformObject.rotation = (Vector3f)context.deserialize(rotationElement, Vector3f.class);
            }

            if (scaleElement != null) {
               transformObject.scale = (Vector3f)context.deserialize(scaleElement, Vector3f.class);
            }

            return transformObject;
         } else {
            throw new JsonSyntaxException("Expected " + json + " to be a CommonTransformObject because it's not an JsonObject");
         }
      }

      public JsonElement serialize(CommonTransformObject src, Type typeOfSrc, JsonSerializationContext context) {
         JsonObject jsonObject = new JsonObject();
         if (src.translation != null) {
            jsonObject.add("translation", context.serialize(src.translation));
         }

         if (src.rotation != null) {
            jsonObject.add("rotation", context.serialize(src.rotation));
         }

         if (src.scale != null) {
            jsonObject.add("scale", context.serialize(src.scale));
         }

         return jsonObject;
      }
   }
}
