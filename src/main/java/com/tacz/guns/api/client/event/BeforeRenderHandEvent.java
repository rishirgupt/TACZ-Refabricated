package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import com.tacz.guns.api.event.common.KubeJSGunEventPoster;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import com.mojang.blaze3d.vertex.PoseStack;

public class BeforeRenderHandEvent extends BaseEvent implements KubeJSGunEventPoster<BeforeRenderHandEvent> {
   private final PoseStack poseStack;
   public static Event<BeforeRenderHandEvent.Callback> CALLBACK = EventFactory.createArrayBacked(BeforeRenderHandEvent.Callback.class, callbacks -> event -> {
      for (BeforeRenderHandEvent.Callback callback : callbacks) {
         callback.post(event);
      }
   });

   public BeforeRenderHandEvent(PoseStack poseStack) {
      this.poseStack = poseStack;
      this.postClientEventToKubeJS(this);
   }

   public PoseStack getPoseStack() {
      return this.poseStack;
   }

   public interface Callback {
      void post(BeforeRenderHandEvent var1);
   }
}
