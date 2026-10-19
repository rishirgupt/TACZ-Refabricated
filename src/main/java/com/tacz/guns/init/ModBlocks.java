package com.tacz.guns.init;

import com.tacz.guns.block.GunSmithTableBlockA;
import com.tacz.guns.block.GunSmithTableBlockB;
import com.tacz.guns.block.GunSmithTableBlockC;
import com.tacz.guns.block.StatueBlock;
import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.block.entity.TargetBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public class ModBlocks {
   public static Block GUN_SMITH_TABLE = registerBlock("gun_smith_table", () -> new GunSmithTableBlockB());
   public static Block WORKBENCH_111 = registerBlock("workbench_a", () -> new GunSmithTableBlockA());
   public static Block WORKBENCH_211 = registerBlock("workbench_b", () -> new GunSmithTableBlockB());
   public static Block WORKBENCH_121 = registerBlock("workbench_c", () -> new GunSmithTableBlockC());
   public static Block TARGET = registerBlock("target", () -> new TargetBlock());
   public static Block STATUE = registerBlock("statue", () -> new StatueBlock());
   public static BlockEntityType<GunSmithTableBlockEntity> GUN_SMITH_TABLE_BE = registerBlockEntity("gun_smith_table", GunSmithTableBlockEntity.TYPE);
   public static BlockEntityType<TargetBlockEntity> TARGET_BE = registerBlockEntity("target", TargetBlockEntity.TYPE);
   public static BlockEntityType<StatueBlockEntity> STATUE_BE = registerBlockEntity("statue", StatueBlockEntity.TYPE);
   public static final TagKey<Block> BULLET_IGNORE_BLOCKS = TagKey.create(
      Registries.BLOCK, Identifier.fromNamespaceAndPath("tacz", "bullet_ignore")
   );

   public static void init() {
   }

   private static Block registerBlock(String name, java.util.function.Supplier<Block> block) {
      return (Block)Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath("tacz", name), cn.sh1rocu.tacz.util.RegistrationContext.block(name, block));
   }

   private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, BlockEntityType<T> blockEntity) {
      return (BlockEntityType<T>)Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath("tacz", name), blockEntity);
   }
}
