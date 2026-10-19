package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.TextureStitchEvent;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.client.sound.SoundPlayManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ReloadResourceEvent {
   public static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.parse("textures/atlas/blocks.png");

   public static void onTextureStitchEventPost(TextureStitchEvent.Post event) {
      if (BLOCK_ATLAS_TEXTURE.equals(event.getAtlas().location())) {
         InternalAssetLoader.onResourceReload();
         SoundPlayManager.clearSoundResourceCache();
      }
   }
}
