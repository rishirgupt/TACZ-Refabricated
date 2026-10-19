package com.tacz.guns.resource.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.minecraft.resources.Identifier;

/** Gson adapter for {@link Identifier}, which no longer ships with its own one. */
public class IdentifierJsonSerializer implements JsonDeserializer<Identifier>, JsonSerializer<Identifier> {
   @Override
   public Identifier deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      String raw = json.getAsString();
      Identifier id = Identifier.tryParse(raw);
      if (id == null) {
         throw new JsonParseException("Invalid identifier: " + raw);
      }

      return id;
   }

   @Override
   public JsonElement serialize(Identifier id, Type type, JsonSerializationContext context) {
      return new JsonPrimitive(id.toString());
   }
}
