package com.tacz.guns.init;

import com.tacz.guns.inventory.GunSmithTableMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModContainer {
   public static final MenuType<GunSmithTableMenu> GUN_SMITH_TABLE_MENU = register("gun_smith_table_menu", GunSmithTableMenu.TYPE);

   public static void init() {
   }

   private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType<T> type) {
      return (MenuType<T>)Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("tacz", name), type);
   }
}
