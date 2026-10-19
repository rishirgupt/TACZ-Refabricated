package com.tacz.guns.client.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tacz.guns.client.resource.pojo.animation.bedrock.SoundEffectKeyframes;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class SoundEffectKeyframesSerializer implements JsonDeserializer<SoundEffectKeyframes> {
   public SoundEffectKeyframes deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      Double2ObjectRBTreeMap<Identifier> keyframes = new Double2ObjectRBTreeMap();
      if (json.isJsonObject()) {
         JsonObject jsonObject = json.getAsJsonObject();

         for (Entry<String, JsonElement> entrySet : jsonObject.entrySet()) {
            double time = Double.parseDouble(entrySet.getKey());
            JsonElement value = entrySet.getValue();
            if (value.isJsonObject()) {
               String soundId = GsonHelper.getAsString(value.getAsJsonObject(), "effect");
               Identifier soundLocation = Identifier.tryParse(soundId);
               if (soundLocation != null) {
                  keyframes.put(time, soundLocation);
               }
            }
         }

         return new SoundEffectKeyframes(keyframes);
      } else {
         return new SoundEffectKeyframes(keyframes);
      }
   }
}
