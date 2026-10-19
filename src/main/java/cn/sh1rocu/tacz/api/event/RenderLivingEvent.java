package cn.sh1rocu.tacz.api.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public abstract class RenderLivingEvent extends BaseEvent {
   private final LivingEntity entity;
   private final float partialTick;
   private final PoseStack poseStack;
   private final SubmitNodeCollector collector;
   private final int packedLight;
   public static final Event<RenderLivingEvent.PostCallback> POST = EventFactory.createArrayBacked(
      RenderLivingEvent.PostCallback.class, callbacks -> event -> {
         for (RenderLivingEvent.PostCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   protected RenderLivingEvent(LivingEntity entity, float partialTick, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
      this.entity = entity;
      this.partialTick = partialTick;
      this.poseStack = poseStack;
      this.collector = collector;
      this.packedLight = packedLight;
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public float getPartialTick() {
      return this.partialTick;
   }

   public PoseStack getPoseStack() {
      return this.poseStack;
   }

   public SubmitNodeCollector getCollector() {
      return this.collector;
   }

   public int getPackedLight() {
      return this.packedLight;
   }

   public static class Post extends RenderLivingEvent {
      public Post(LivingEntity entity, float partialTick, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
         super(entity, partialTick, poseStack, collector, packedLight);
      }
   }

   public interface PostCallback {
      void post(RenderLivingEvent.Post event);
   }
}
