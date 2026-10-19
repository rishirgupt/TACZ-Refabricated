package com.tacz.guns.client.render26;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Records immediate-mode geometry and replays it through the 26.2 submit queue.
 *
 * Effect code transforms its vertices with the pose stack itself, so what gets recorded is already in
 * camera-relative space; the replay therefore uses an identity pose.
 */
public final class RecordingBufferSource implements MultiBufferSource {
    private static final int FLOATS = 8;
    private static final int INTS = 3;
    private final Map<RenderType, Recorder> recorders = new LinkedHashMap<>();
    private final java.util.List<java.util.function.Consumer<net.minecraft.client.renderer.SubmitNodeCollector>> deferred =
            new java.util.ArrayList<>();

    /** Queues a submission that is not plain geometry (a block or item model) to run at flush time. */
    public void defer(java.util.function.Consumer<net.minecraft.client.renderer.SubmitNodeCollector> task) {
        deferred.add(task);
    }

    /** Flushes geometry and queued submissions; use this overload when blocks or items may have been drawn. */
    public void flush(net.minecraft.client.renderer.SubmitNodeCollector renderTasks) {
        for (java.util.function.Consumer<net.minecraft.client.renderer.SubmitNodeCollector> task : deferred) {
            task.accept(renderTasks);
        }
        deferred.clear();
        flush((OrderedSubmitNodeCollector) renderTasks);
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return recorders.computeIfAbsent(renderType, type -> new Recorder());
    }

    /** Drops everything recorded so far without drawing it. */
    public void discard() {
        recorders.clear();
        deferred.clear();
    }

    public boolean isEmpty() {
        for (Recorder recorder : recorders.values()) {
            if (recorder.count > 0) {
                return false;
            }
        }
        return true;
    }

    /** Submits everything recorded so far and clears the recording. */
    public void flush(OrderedSubmitNodeCollector renderTasks) {
        if (recorders.isEmpty()) {
            return;
        }
        PoseStack identity = new PoseStack();
        for (Map.Entry<RenderType, Recorder> entry : recorders.entrySet()) {
            Recorder recorder = entry.getValue();
            if (recorder.count == 0) {
                continue;
            }
            final float[] floats = Arrays.copyOf(recorder.floats, recorder.count * FLOATS);
            final int[] ints = Arrays.copyOf(recorder.ints, recorder.count * INTS);
            final int vertices = recorder.count;
            renderTasks.submitCustomGeometry(identity, entry.getKey(),
                    (pose, buffer) -> replay(floats, ints, vertices, buffer));
        }
        recorders.clear();
    }

    private static void replay(float[] floats, int[] ints, int vertices, VertexConsumer buffer) {
        for (int i = 0; i < vertices; i++) {
            int f = i * FLOATS;
            int n = i * INTS;
            buffer.addVertex(floats[f], floats[f + 1], floats[f + 2])
                    .setColor(ints[n])
                    .setUv(floats[f + 3], floats[f + 4])
                    .setOverlay(ints[n + 1])
                    .setLight(ints[n + 2])
                    .setNormal(floats[f + 5], floats[f + 6], floats[f + 7]);
        }
    }

    private static final class Recorder implements VertexConsumer {
        private float[] floats = new float[FLOATS * 64];
        private int[] ints = new int[INTS * 64];
        private int count;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            if ((count + 1) * FLOATS > floats.length) {
                floats = Arrays.copyOf(floats, floats.length * 2);
                ints = Arrays.copyOf(ints, ints.length * 2);
            }
            int f = count * FLOATS;
            int n = count * INTS;
            floats[f] = x;
            floats[f + 1] = y;
            floats[f + 2] = z;
            floats[f + 3] = 0.0F;
            floats[f + 4] = 0.0F;
            floats[f + 5] = 0.0F;
            floats[f + 6] = 1.0F;
            floats[f + 7] = 0.0F;
            ints[n] = 0xFFFFFFFF;
            ints[n + 1] = OverlayTexture.NO_OVERLAY;
            ints[n + 2] = 0xF000F0;
            count++;
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return setColor((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
        }

        @Override
        public VertexConsumer setColor(int color) {
            if (count > 0) {
                ints[(count - 1) * INTS] = color;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            if (count > 0) {
                floats[(count - 1) * FLOATS + 3] = u;
                floats[(count - 1) * FLOATS + 4] = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            if (count > 0) {
                ints[(count - 1) * INTS + 1] = (u & 0xFFFF) | (v & 0xFFFF) << 16;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            if (count > 0) {
                ints[(count - 1) * INTS + 2] = (u & 0xFFFF) | (v & 0xFFFF) << 16;
            }
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            if (count > 0) {
                floats[(count - 1) * FLOATS + 5] = x;
                floats[(count - 1) * FLOATS + 6] = y;
                floats[(count - 1) * FLOATS + 7] = z;
            }
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}
