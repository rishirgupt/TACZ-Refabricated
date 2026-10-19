package com.tacz.guns.resource.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tacz.guns.GunMod;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class RecipeFilter {
   private final List<IFilter<Identifier>> whitelist = new ArrayList<>();
   private final List<IFilter<Identifier>> blacklist = new ArrayList<>();

   public void merge(RecipeFilter other) {
      this.whitelist.addAll(other.whitelist);
      this.blacklist.addAll(other.blacklist);
   }

   public boolean contains(Identifier location) {
      boolean allowed = this.whitelist.isEmpty();

      for (IFilter<Identifier> filter : this.whitelist) {
         if (filter.test(location)) {
            allowed = true;
            break;
         }
      }

      for (IFilter<Identifier> filter : this.blacklist) {
         if (filter.test(location)) {
            allowed = false;
            break;
         }
      }

      return allowed;
   }

   public List<Identifier> filter(List<Identifier> input) {
      List<Identifier> output = new ArrayList<>();

      for (Identifier location : input) {
         if (this.contains(location)) {
            output.add(location);
         }
      }

      return output;
   }

   public <T> List<T> filter(List<T> input, Function<T, Identifier> getter) {
      List<T> output = new ArrayList<>();

      for (T entry : input) {
         if (this.contains(getter.apply(entry))) {
            output.add(entry);
         }
      }

      return output;
   }

   public static class Deserializer implements JsonDeserializer<RecipeFilter>, JsonSerializer<RecipeFilter> {
      public RecipeFilter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         RecipeFilter filter = new RecipeFilter();
         JsonArray wl = json.getAsJsonObject().getAsJsonArray("whitelist");
         if (wl != null) {
            this.loadFilters(wl, filter.whitelist);
         }

         JsonArray bl = json.getAsJsonObject().getAsJsonArray("blacklist");
         if (bl != null) {
            this.loadFilters(bl, filter.blacklist);
         }

         return filter;
      }

      private void loadFilters(JsonArray array, @NotNull List<IFilter<Identifier>> list) {
         LiteralFilter.Builder<Identifier> builder = new LiteralFilter.Builder<>();

         for (JsonElement element : array) {
            if (!element.isJsonPrimitive()) {
               throw new JsonParseException("Invalid recipe filter entry: " + element);
            }

            String entry = element.getAsString();
            if (entry.startsWith("^")) {
               try {
                  list.add(new RegexFilter<>(entry));
               } catch (PatternSyntaxException e) {
                  GunMod.LOGGER.error("Failed to parse regex filter: {}", entry, e);
               }
            } else {
               Identifier rl = Identifier.tryParse(entry);
               if (rl != null) {
                  builder.add(rl);
               }
            }
         }

         list.add(builder.build());
      }

      public JsonElement serialize(RecipeFilter filter, Type typeOfSrc, JsonSerializationContext context) {
         JsonObject json = new JsonObject();
         json.add("whitelist", context.serialize(this.toList(filter.whitelist)));
         json.add("blacklist", context.serialize(this.toList(filter.blacklist)));
         return json;
      }

      private List<String> toList(List<IFilter<Identifier>> filter) {
         List<String> list = new ArrayList<>();

         for (IFilter<Identifier> f : filter) {
            if (f instanceof LiteralFilter<Identifier> literalFilter) {
               list.addAll(literalFilter.getSet().stream().map(Identifier::toString).toList());
            } else if (f instanceof RegexFilter<Identifier> regexFilter) {
               list.add(regexFilter.getPattern().pattern());
            }
         }

         return list;
      }
   }
}
