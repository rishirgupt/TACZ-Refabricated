package cn.sh1rocu.tacz.api.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import com.mojang.blaze3d.audio.Library.Pool;

public interface ChannelAccessHandleInjection {
   void tacz$setPool(Pool var1);

   void tacz$setSoundInstance(SoundInstance var1);

   void tacz$setSoundEngine(SoundEngine var1);
}
