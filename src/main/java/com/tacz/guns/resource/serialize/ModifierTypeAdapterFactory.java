package com.tacz.guns.resource.serialize;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import java.io.IOException;

public class ModifierTypeAdapterFactory implements TypeAdapterFactory {
   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() != Modifier.class) {
         return null;
      }

      final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
      return new TypeAdapter<T>() {
         public void write(JsonWriter out, T value) throws IOException {
            delegate.write(out, value);
         }

         public T read(JsonReader in) throws IOException {
            T value = (T)delegate.read(in);
            if (value instanceof Modifier modifier) {
               modifier.compileFunction();
            }

            return value;
         }
      };
   }
}
