package com.tacz.guns.util;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;

/** Gun packs are hand written, so their JSON is parsed leniently (comments, trailing commas). */
public final class LenientJson {
   private LenientJson() {
   }

   public static <T> T fromJson(Gson gson, Reader reader, Class<T> type) {
      try {
         JsonReader jsonReader = new JsonReader(reader);
         jsonReader.setStrictness(Strictness.LENIENT);
         return gson.getAdapter(type).read(jsonReader);
      } catch (IOException e) {
         throw new JsonParseException(e);
      }
   }
}
