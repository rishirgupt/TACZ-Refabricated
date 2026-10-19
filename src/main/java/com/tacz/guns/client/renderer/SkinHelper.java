package com.tacz.guns.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

public final class SkinHelper {
   private SkinHelper() {
   }

   public static Identifier skin(ResolvableProfile profile) {
      PlayerSkin skin = Minecraft.getInstance().getSkinManager().createLookup(profile.partialProfile(), false).get();
      return skin.body().texturePath();
   }
}
