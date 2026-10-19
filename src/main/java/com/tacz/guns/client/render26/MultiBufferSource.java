package com.tacz.guns.client.render26;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Stand-in for the vanilla buffer source that 26.2 removed. Gun and effect code asks it for a vertex consumer
 * per render type exactly as before; {@link RecordingBufferSource} keeps what was drawn and
 * {@link GunRender} hands it to the 26.2 submit queue.
 */
public interface MultiBufferSource {
   VertexConsumer getBuffer(RenderType renderType);

   /** Nothing is drawn until the outermost render entry finishes, so these are kept only for source compatibility. */
   default void endBatch() {
   }

   default void endBatch(RenderType renderType) {
   }
}
