package com.tacz.guns.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;

/**
 * Gun packs written for older versions give item stack components as strings
 * ({@code "minecraft:custom_data": "{GunId: \"tacz:ak47\"}"}). Item stack codecs now want real NBT, so such strings are
 * converted in place before a stack is parsed.
 */
public final class LegacyItemJson {
   private LegacyItemJson() {
   }

   public static JsonElement fix(JsonElement element) {
      if (element != null && element.isJsonObject()) {
         JsonObject object = element.getAsJsonObject();
         for (Map.Entry<String, JsonElement> entry : new ArrayList<>(object.entrySet())) {
            JsonElement value = entry.getValue();
            if (isSnbtComponent(entry.getKey(), value)) {
               try {
                  CompoundTag tag = TagParser.parseCompoundFully(value.getAsString());
                  object.add(entry.getKey(), NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag));
               } catch (Exception ignored) {
               }
            } else {
               fix(value);
            }
         }
      }

      return element;
   }

   private static boolean isSnbtComponent(String key, JsonElement value) {
      return ("minecraft:custom_data".equals(key) || "minecraft:entity_data".equals(key) || "minecraft:block_entity_data".equals(key))
         && value.isJsonPrimitive()
         && value.getAsJsonPrimitive().isString();
   }
}
