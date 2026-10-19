package com.tacz.guns.client.render26;

import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.Nullable;

/**
 * Bridge between TACZ's immediate-mode gun drawing and the 26.2 submit pipeline.
 *
 * A render entry point (first-person hands, an item model, a block entity, an entity) calls {@link #begin}
 * with the collector it was given, draws through {@link #bufferSource()}, and calls {@link #end}, which
 * submits everything that was drawn.
 */
public final class GunRender {
   private static final RecordingBufferSource SOURCE = new RecordingBufferSource();
   private static SubmitNodeCollector collector;
   private static int depth;

   private GunRender() {
   }

   public static MultiBufferSource bufferSource() {
      return SOURCE;
   }

   @Nullable
   public static SubmitNodeCollector collector() {
      return collector;
   }

   public static void begin(SubmitNodeCollector submitNodeCollector) {
      if (depth++ == 0) {
         collector = submitNodeCollector;
      }
   }

   public static void end() {
      if (--depth == 0) {
         if (collector != null) {
            SOURCE.flush(collector);
         }

         collector = null;
      } else if (depth < 0) {
         depth = 0;
      }
   }

   /** Submits what has been drawn so far without leaving the current render entry (used between render phases). */
   /** Throws away what has been drawn so far (used when the gun is hidden but its transforms are still needed). */
   public static void discardPending() {
      SOURCE.discard();
   }

   public static void flushNow() {
      if (collector != null) {
         SOURCE.flush(collector);
      }
   }
}
