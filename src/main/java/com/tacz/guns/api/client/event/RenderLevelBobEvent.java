package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import com.tacz.guns.api.event.common.KubeJSGunEventPoster;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class RenderLevelBobEvent extends BaseEvent implements KubeJSGunEventPoster<RenderLevelBobEvent> {
   public static final Event<RenderLevelBobEvent.HurtCallback> HURT = EventFactory.createArrayBacked(
      RenderLevelBobEvent.HurtCallback.class, callbacks -> event -> {
         for (RenderLevelBobEvent.HurtCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );
   public static final Event<RenderLevelBobEvent.ViewCallback> VIEW = EventFactory.createArrayBacked(
      RenderLevelBobEvent.ViewCallback.class, callbacks -> event -> {
         for (RenderLevelBobEvent.ViewCallback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public static class BobHurt extends RenderLevelBobEvent implements ICancellableEvent {
      public BobHurt() {
         this.postClientEventToKubeJS(this);
      }
   }

   public static class BobView extends RenderLevelBobEvent implements ICancellableEvent {
      public BobView() {
         this.postClientEventToKubeJS(this);
      }
   }

   public interface HurtCallback {
      void post(RenderLevelBobEvent.BobHurt var1);
   }

   public interface ViewCallback {
      void post(RenderLevelBobEvent.BobView var1);
   }
}
