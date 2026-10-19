package com.tacz.guns.client.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Type;
import net.minecraft.world.item.ItemStack;

public class ItemStackSerializer implements JsonDeserializer<ItemStack> {
   public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      if (json.isJsonObject()) {
         JsonObject jsonObject = json.getAsJsonObject();
         return (ItemStack)ItemStack.CODEC.parse(JsonOps.INSTANCE, com.tacz.guns.util.LegacyItemJson.fix(jsonObject)).getOrThrow();
      } else {
         throw new JsonSyntaxException("Expected " + json + " to be a ItemStack because it's not an object");
      }
   }
}
