package com.tacz.guns.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import java.lang.reflect.Type;

public class DistanceDamagePairSerializer implements JsonDeserializer<ExtraDamage.DistanceDamagePair> {
   private static final String INFINITE = "infinite";

   public ExtraDamage.DistanceDamagePair deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      if (!json.isJsonObject()) {
         throw new JsonSyntaxException("Expected " + json + " to be a DistanceDamagePair because it's not an object");
      }

      JsonObject jsonObject = json.getAsJsonObject();
      if (!jsonObject.has("distance")) {
         throw new JsonSyntaxException("Expected " + json + " to be a DistanceDamagePair because it's not has distance field");
      }

      if (!jsonObject.has("damage")) {
         throw new JsonSyntaxException("Expected " + json + " to be a DistanceDamagePair because it's not has damage field");
      }

      if (!jsonObject.get("distance").isJsonPrimitive()) {
         throw new JsonSyntaxException("Expected " + json + " to be a DistanceDamagePair because it distance field is not a string or number");
      }

      if (!jsonObject.get("damage").isJsonPrimitive()) {
         throw new JsonSyntaxException("Expected " + json + " to be a DistanceDamagePair because it damage field is not a number");
      }

      float distance = 0.0F;
      JsonPrimitive jsonPrimitive = jsonObject.get("distance").getAsJsonPrimitive();
      if (jsonPrimitive.isNumber()) {
         distance = jsonPrimitive.getAsFloat();
      } else if (jsonPrimitive.isString()) {
         if (!"infinite".equals(jsonPrimitive.getAsString())) {
            throw new JsonSyntaxException("Expected " + json + " to be a DistanceDamagePair because it distance field is not is 'infinite'");
         }

         distance = Float.MAX_VALUE;
      }

      float damage = jsonObject.get("damage").getAsFloat();
      return new ExtraDamage.DistanceDamagePair(distance, damage);
   }
}
