package com.tacz.guns.client.resource.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.resource.pojo.animation.bedrock.AnimationKeyframes;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;

public class AnimationKeyframesSerializer implements JsonDeserializer<AnimationKeyframes> {
   public AnimationKeyframes deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      Double2ObjectRBTreeMap<AnimationKeyframes.Keyframe> keyframes = new Double2ObjectRBTreeMap();
      if (json.isJsonPrimitive()) {
         if (json.getAsJsonPrimitive().isString()) {
            GunMod.LOGGER.debug("Molang is not supported: \"{}\"", json.getAsString());
            return new AnimationKeyframes(keyframes);
         } else {
            float value = json.getAsJsonPrimitive().getAsFloat();
            Vector3f data = new Vector3f(value, value, value);
            AnimationKeyframes.Keyframe keyframe = new AnimationKeyframes.Keyframe(null, null, data, null);
            keyframes.put(0.0, keyframe);
            return new AnimationKeyframes(keyframes);
         }
      } else {
         if (json.isJsonArray()) {
            Vector3f data = this.readVector3f(json.getAsJsonArray());
            AnimationKeyframes.Keyframe keyframe = new AnimationKeyframes.Keyframe(null, null, data, null);
            keyframes.put(0.0, keyframe);
            return new AnimationKeyframes(keyframes);
         }

         if (!json.isJsonObject()) {
            return new AnimationKeyframes(keyframes);
         }

         JsonObject jsonObject = json.getAsJsonObject();

         for (Entry<String, JsonElement> entrySet : jsonObject.entrySet()) {
            double time;
            try {
               time = Double.parseDouble(entrySet.getKey());
            } catch (NumberFormatException e) {
               GunMod.LOGGER.debug("Molang is not supported: \"{}\"", entrySet.getKey());
               return new AnimationKeyframes(keyframes);
            }

            AnimationKeyframes.Keyframe keyframe = this.readKeyFrames(entrySet.getValue());
            keyframes.put(time, keyframe);
         }

         return new AnimationKeyframes(keyframes);
      }
   }

   private AnimationKeyframes.Keyframe readKeyFrames(JsonElement element) {
      if (element.isJsonArray()) {
         Vector3f data = this.readVector3f(element.getAsJsonArray());
         return new AnimationKeyframes.Keyframe(null, null, data, null);
      }

      if (element.isJsonObject()) {
         JsonObject jsonObject = element.getAsJsonObject();
         String lerpMode = null;
         Vector3f pre = null;
         Vector3f post = null;
         if (jsonObject.has("lerp_mode")) {
            lerpMode = jsonObject.get("lerp_mode").getAsString();
         }

         if (jsonObject.has("pre") && jsonObject.get("pre").isJsonArray()) {
            JsonArray array = jsonObject.get("pre").getAsJsonArray();
            pre = this.readVector3f(array);
         }

         if (jsonObject.has("post") && jsonObject.get("post").isJsonArray()) {
            JsonArray array = jsonObject.get("post").getAsJsonArray();
            post = this.readVector3f(array);
         }

         return new AnimationKeyframes.Keyframe(pre, post, null, lerpMode);
      } else {
         return new AnimationKeyframes.Keyframe(null, null, null, null);
      }
   }

   private Vector3f readVector3f(JsonArray array) {
      JsonElement xElement = array.get(0);
      JsonElement yElement = array.get(1);
      JsonElement zElement = array.get(2);
      float x = this.readVector3fElement(xElement, "(array i=0)");
      float y = this.readVector3fElement(yElement, "(array i=1)");
      float z = this.readVector3fElement(zElement, "(array i=2)");
      return new Vector3f(x, y, z);
   }

   private float readVector3fElement(JsonElement element, String memberName) {
      if (element.getAsJsonPrimitive().isString()) {
         GunMod.LOGGER.debug("Molang is not supported: \"{}\"", element.getAsString());
         return 0.0F;
      } else {
         return GsonHelper.convertToFloat(element, memberName);
      }
   }
}
