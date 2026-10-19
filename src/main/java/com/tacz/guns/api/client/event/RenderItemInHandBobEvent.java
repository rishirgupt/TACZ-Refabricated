package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import com.tacz.guns.api.event.common.KubeJSGunEventPoster;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class RenderItemInHandBobEvent extends BaseEvent implements KubeJSGunEventPoster<RenderItemInHandBobEvent> {
   public static final Event<RenderItemInHandBobEvent.HurtCallback> HURT = EventFactory.createArrayBacked(
      RenderItemInHandBobEvent.HurtCallback.class, callbacks -> event -> {
         for (RenderItemInHandBobEvent.HurtCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );
   public static final Event<RenderItemInHandBobEvent.ViewCallback> VIEW = EventFactory.createArrayBacked(
      RenderItemInHandBobEvent.ViewCallback.class, callbacks -> event -> {
         for (RenderItemInHandBobEvent.ViewCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public static class BobHurt extends RenderItemInHandBobEvent implements ICancellableEvent {
      public BobHurt() {
         this.postClientEventToKubeJS(this);
      }
   }

   public static class BobView extends RenderItemInHandBobEvent implements ICancellableEvent {
      public BobView() {
         this.postClientEventToKubeJS(this);
      }
   }

   public interface HurtCallback {
      void post(RenderItemInHandBobEvent.BobHurt var1);
   }

   public interface ViewCallback {
      void post(RenderItemInHandBobEvent.BobView var1);
   }
}
