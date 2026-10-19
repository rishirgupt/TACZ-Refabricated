package com.tacz.guns.resource.pojo.data.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.tacz.guns.GunMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.Holder;

public record LootTableInjection(List<Identifier> lootTables, LootTable lootTable, Identifier id) {
   public static LootTableInjection fromJson(Identifier fileId, JsonElement element) {
      JsonObject object = GsonHelper.convertToJsonObject(element, "loot injection");
      List<Identifier> lootTables = readLootTables(fileId, object);
      if (!object.has("pools")) {
         throw new JsonParseException("Loot injection " + fileId + " must define pools");
      }

      LootTable lootTable = LootTable.CODEC
         .parse(JsonOps.INSTANCE, object)
         .resultOrPartial(GunMod.LOGGER::error)
         .<LootTable>map(Holder::value)
         .orElse(LootTable.EMPTY);
      return new LootTableInjection(lootTables, lootTable, fileId);
   }

   private static List<Identifier> readLootTables(Identifier fileId, JsonObject object) {
      List<Identifier> lootTables = new ArrayList<>();
      if (object.has("loot_tables")) {
         for (JsonElement table : GsonHelper.getAsJsonArray(object, "loot_tables")) {
            lootTables.add(Identifier.parse(GsonHelper.convertToString(table, "loot table")));
         }
      } else {
         if (!object.has("loot_table")) {
            throw new JsonParseException("Loot injection " + fileId + " must define loot_table or loot_tables");
         }

         lootTables.add(Identifier.parse(GsonHelper.getAsString(object, "loot_table")));
      }

      return List.copyOf(lootTables);
   }

   public List<ItemStack> createStacks(LootContext context) {
      List<ItemStack> stacks = new ArrayList<>();
      this.lootTable.getRandomItemsRaw(context, stacks::add);
      return stacks;
   }
}
