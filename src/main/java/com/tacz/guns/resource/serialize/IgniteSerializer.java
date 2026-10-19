package com.tacz.guns.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import java.lang.reflect.Type;
import net.minecraft.util.GsonHelper;

public class IgniteSerializer implements JsonDeserializer<Ignite> {
   public Ignite deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      if (GsonHelper.isBooleanValue(json)) {
         boolean ignite = GsonHelper.convertToBoolean(json, "ignite");
         return new Ignite(ignite);
      } else if (json.isJsonObject()) {
         JsonObject jsonObject = json.getAsJsonObject();
         boolean igniteEntity = GsonHelper.getAsBoolean(jsonObject, "entity", false);
         boolean igniteBlock = GsonHelper.getAsBoolean(jsonObject, "block", false);
         return new Ignite(igniteEntity, igniteBlock);
      } else {
         throw new JsonSyntaxException("Expected " + json + " to be a Pair because it's not an array");
      }
   }
}
