package com.tacz.guns.client.model.papi;

import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class PlayerNamePapi implements Function<ItemStack, String> {
   public static final String NAME = "player_name";

   public String apply(ItemStack stack) {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null ? player.getName().getString() : "";
   }
}
