package cn.sh1rocu.tacz.api.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Environment(EnvType.CLIENT)
public class TextureStitchEvent extends BaseEvent {
   private final TextureAtlas atlas;
   public static final Event<TextureStitchEvent.PostCallback> POST = EventFactory.createArrayBacked(
      TextureStitchEvent.PostCallback.class, callbacks -> event -> {
         for (TextureStitchEvent.PostCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public TextureStitchEvent(TextureAtlas atlas) {
      this.atlas = atlas;
   }

   public TextureAtlas getAtlas() {
      return this.atlas;
   }

   public static class Post extends TextureStitchEvent {
      public Post(TextureAtlas map) {
         super(map);
      }
   }

   public interface PostCallback {
      void post(TextureStitchEvent.Post var1);
   }
}
