package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import com.tacz.guns.api.event.common.KubeJSGunEventPoster;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class SwapItemWithOffHand extends BaseEvent implements KubeJSGunEventPoster<SwapItemWithOffHand> {
   public static final Event<SwapItemWithOffHand.Callback> CALLBACK = EventFactory.createArrayBacked(
      SwapItemWithOffHand.Callback.class, callbacks -> event -> {
         for (SwapItemWithOffHand.Callback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public SwapItemWithOffHand() {
      this.postClientEventToKubeJS(this);
   }

   public interface Callback {
      void post(SwapItemWithOffHand var1);
   }
}
