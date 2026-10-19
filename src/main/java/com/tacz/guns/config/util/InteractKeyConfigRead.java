package com.tacz.guns.config.util;

import com.google.common.collect.Lists;
import com.tacz.guns.config.sync.SyncConfig;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.apache.commons.lang3.StringUtils;

public class InteractKeyConfigRead {
   private static final EnumMap<InteractKeyConfigRead.Type, List<Identifier>> WHITELIST = new EnumMap<>(InteractKeyConfigRead.Type.class);
   private static final EnumMap<InteractKeyConfigRead.Type, List<Identifier>> BLACKLIST = new EnumMap<>(InteractKeyConfigRead.Type.class);
   private static final TagKey<Block> WHITELIST_BLOCKS = TagKey.create(
      Registries.BLOCK, Identifier.fromNamespaceAndPath("tacz", "interact_key/whitelist")
   );
   private static final TagKey<Block> BLACKLIST_BLOCKS = TagKey.create(
      Registries.BLOCK, Identifier.fromNamespaceAndPath("tacz", "interact_key/blacklist")
   );
   private static final TagKey<EntityType<?>> WHITELIST_ENTITIES = TagKey.create(
      Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("tacz", "interact_key/whitelist")
   );
   private static final TagKey<EntityType<?>> BLACKLIST_ENTITIES = TagKey.create(
      Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("tacz", "interact_key/blacklist")
   );

   public static void init() {
      WHITELIST.clear();
      BLACKLIST.clear();
      handleConfigData((List<String>)SyncConfig.INTERACT_KEY_WHITELIST_BLOCKS.get(), WHITELIST, InteractKeyConfigRead.Type.BLOCK);
      handleConfigData((List<String>)SyncConfig.INTERACT_KEY_WHITELIST_ENTITIES.get(), WHITELIST, InteractKeyConfigRead.Type.ENTITY);
      handleConfigData((List<String>)SyncConfig.INTERACT_KEY_BLACKLIST_BLOCKS.get(), BLACKLIST, InteractKeyConfigRead.Type.BLOCK);
      handleConfigData((List<String>)SyncConfig.INTERACT_KEY_BLACKLIST_ENTITIES.get(), BLACKLIST, InteractKeyConfigRead.Type.ENTITY);
   }

   public static boolean canInteractBlock(BlockState block) {
      Identifier blockId = BuiltInRegistries.BLOCK.getKey(block.getBlock());
      if (blockId == null) {
         return false;
      } else if (BLACKLIST.containsKey(InteractKeyConfigRead.Type.BLOCK) && BLACKLIST.get(InteractKeyConfigRead.Type.BLOCK).contains(blockId)) {
         return false;
      } else if (block.is(BLACKLIST_BLOCKS)) {
         return false;
      } else {
         return WHITELIST.containsKey(InteractKeyConfigRead.Type.BLOCK) && WHITELIST.get(InteractKeyConfigRead.Type.BLOCK).contains(blockId)
            ? true
            : block.is(WHITELIST_BLOCKS);
      }
   }

   public static boolean canInteractEntity(Entity entity) {
      Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      if (entityId == null) {
         return false;
      } else if (BLACKLIST.containsKey(InteractKeyConfigRead.Type.ENTITY) && BLACKLIST.get(InteractKeyConfigRead.Type.ENTITY).contains(entityId)) {
         return false;
      } else if (entity.getType().builtInRegistryHolder().is(BLACKLIST_ENTITIES)) {
         return false;
      } else {
         return WHITELIST.containsKey(InteractKeyConfigRead.Type.ENTITY) && WHITELIST.get(InteractKeyConfigRead.Type.ENTITY).contains(entityId)
            ? true
            : entity.getType().builtInRegistryHolder().is(WHITELIST_ENTITIES);
      }
   }

   private static void handleConfigData(
      List<String> configData, EnumMap<InteractKeyConfigRead.Type, List<Identifier>> storeList, InteractKeyConfigRead.Type type
   ) {
      configData.forEach(data -> {
         if (!data.isEmpty()) {
            if (!StringUtils.isBlank(data)) {
               Identifier id = Identifier.parse(data);
               storeList.computeIfAbsent(type, t -> Lists.newArrayList()).add(id);
            }
         }
      });
   }

   public enum Type {
      BLOCK,
      ENTITY;
   }
}
