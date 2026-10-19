package com.tacz.guns.loot;

import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

public class LootTableInjectorModifier {
   @NotNull
   public static ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context, LootTable table) {
      CommonAssetsManager manager = CommonAssetsManager.getInstance();
      if (manager == null) {
         return generatedLoot;
      }

      Identifier lootTableId = lookupId(context, table);
      if (lootTableId == null) {
         return generatedLoot;
      }

      List<LootTableInjection> injections = manager.getLootTableInjections(lootTableId);
      if (injections.isEmpty()) {
         return generatedLoot;
      }

      for (LootTableInjection injection : injections) {
         generatedLoot.addAll(injection.createStacks(context));
      }

      return generatedLoot;
   }

   private static final java.util.Map<LootTable, Identifier> ID_CACHE = new java.util.IdentityHashMap<>();
   private static Object cacheOwner;

   @javax.annotation.Nullable
   private static synchronized Identifier lookupId(LootContext context, LootTable table) {
      net.minecraft.core.HolderLookup.Provider provider = context.getLevel().getServer().reloadableRegistries().lookup();
      if (cacheOwner != provider) {
         ID_CACHE.clear();
         provider.lookupOrThrow(Registries.LOOT_TABLE).listElements().forEach(holder -> ID_CACHE.put(holder.value(), holder.key().identifier()));
         cacheOwner = provider;
      }

      return ID_CACHE.get(table);
   }
}
