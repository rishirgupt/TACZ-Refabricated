package cn.sh1rocu.tacz.api.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import com.mojang.blaze3d.audio.Channel;

@Environment(EnvType.CLIENT)
public class PlaySoundSourceEvent extends BaseEvent {
   private final SoundEngine engine;
   private final String name;
   private final SoundInstance sound;
   private final Channel channel;
   public static final Event<PlaySoundSourceEvent.Callback> CALLBACK = EventFactory.createArrayBacked(
      PlaySoundSourceEvent.Callback.class, callbacks -> event -> {
         for (PlaySoundSourceEvent.Callback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public PlaySoundSourceEvent(SoundEngine engine, SoundInstance sound, Channel channel) {
      this.engine = engine;
      this.name = sound.getIdentifier().getPath();
      this.sound = sound;
      this.channel = channel;
   }

   public SoundEngine getEngine() {
      return this.engine;
   }

   public SoundInstance getSound() {
      return this.sound;
   }

   public Channel getChannel() {
      return this.channel;
   }

   public String getName() {
      return this.name;
   }

   public interface Callback {
      void post(PlaySoundSourceEvent var1);
   }
}
