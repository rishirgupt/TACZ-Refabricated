package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.event.BaseEvent;

public interface KubeJSGunEventPoster<E extends BaseEvent> {
   default void postEventToKubeJS(E event) {
   }

   default void postClientEventToKubeJS(E event) {
   }

   default void postServerEventToKubeJS(E event) {
   }
}
