package com.tacz.guns.init;

import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.AttachmentItem;
import com.tacz.guns.item.DefaultTableItem;
import com.tacz.guns.item.GunSmithTableItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.item.TargetMinecartItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item.Properties;

public class ModItems {
   public static ModernKineticGunItem MODERN_KINETIC_GUN = register("modern_kinetic_gun", () -> new ModernKineticGunItem());
   public static Item AMMO = register("ammo", () -> new AmmoItem());
   public static AttachmentItem ATTACHMENT = register("attachment", () -> new AttachmentItem());
   public static GunSmithTableItem GUN_SMITH_TABLE = register("gun_smith_table", () -> new DefaultTableItem(ModBlocks.GUN_SMITH_TABLE));
   public static GunSmithTableItem WORKBENCH_111 = register("workbench_a", () -> new GunSmithTableItem(ModBlocks.WORKBENCH_111));
   public static GunSmithTableItem WORKBENCH_211 = register("workbench_b", () -> new GunSmithTableItem(ModBlocks.WORKBENCH_211));
   public static GunSmithTableItem WORKBENCH_121 = register("workbench_c", () -> new GunSmithTableItem(ModBlocks.WORKBENCH_121));
   public static Item TARGET = register("target", () -> new BlockItem(ModBlocks.TARGET, new Properties().useBlockDescriptionPrefix()));
   public static Item STATUE = register("statue", () -> new BlockItem(ModBlocks.STATUE, new Properties().useBlockDescriptionPrefix()));
   public static Item AMMO_BOX = register("ammo_box", () -> new AmmoBoxItem());
   public static Item TARGET_MINECART = register("target_minecart", () -> new TargetMinecartItem());

   public static void init() {
      GunItemManager.registerGunItem("modern_kinetic", MODERN_KINETIC_GUN);
   }

   private static <T extends Item> T register(String name, java.util.function.Supplier<T> item) {
      return (T)Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath("tacz", name), cn.sh1rocu.tacz.util.RegistrationContext.item(name, item));
   }
}
