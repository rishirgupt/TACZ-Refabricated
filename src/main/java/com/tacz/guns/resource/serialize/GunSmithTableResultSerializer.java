package com.tacz.guns.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.crafting.result.RawGunTableResult;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.tacz.guns.resource.pojo.data.recipe.GunResult;
import java.lang.reflect.Type;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class GunSmithTableResultSerializer implements JsonDeserializer<GunSmithTableResult> {
   public GunSmithTableResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      if (json.isJsonObject()) {
         JsonObject jsonObject = json.getAsJsonObject();
         String typeName = GsonHelper.getAsString(jsonObject, "type");
         int count = 1;
         CompoundTag extraTag = null;
         Identifier tabOverride = null;
         if (jsonObject.has("count")) {
            count = Math.max(GsonHelper.getAsInt(jsonObject, "count"), 1);
         }

         if (jsonObject.has("nbt")) {
            extraTag = (CompoundTag)CompoundTag.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("nbt")).getOrThrow();
         }

         if (jsonObject.has("group")) {
            String raw = GsonHelper.getAsString(jsonObject, "group");
            if (!raw.contains(":")) {
               raw = "tacz:" + raw;
            }

            tabOverride = Identifier.tryParse(raw);
         }

         GunSmithTableResult result;
         switch (typeName) {
            case "gun":
            case "ammo":
            case "attachment":
               RawGunTableResult raw = new RawGunTableResult(typeName, this.getId(jsonObject), count);
               if (extraTag != null) {
                  raw.setNbt(extraTag);
               }

               if (typeName.equals("gun")) {
                  GunResult gunResult = (GunResult)CommonAssetsManager.GSON.fromJson(jsonObject, GunResult.class);
                  if (gunResult != null) {
                     raw.setExtraData(gunResult);
                  }
               }

               result = new GunSmithTableResult(raw, tabOverride);
               break;
            case "custom":
               JsonObject resultObject = GsonHelper.getAsJsonObject(jsonObject, "item");
               ItemStack itemStack = (ItemStack)ItemStack.CODEC.parse(JsonOps.INSTANCE, com.tacz.guns.util.LegacyItemJson.fix(resultObject)).getOrThrow();
               result = new GunSmithTableResult(itemStack, tabOverride);
               break;
            default:
               return new GunSmithTableResult(ItemStack.EMPTY, TabConfig.TAB_EMPTY);
         }

         return result;
      } else {
         return new GunSmithTableResult(ItemStack.EMPTY, TabConfig.TAB_EMPTY);
      }
   }

   private Identifier getId(JsonObject jsonObject) {
      return Identifier.parse(GsonHelper.getAsString(jsonObject, "id"));
   }
}
