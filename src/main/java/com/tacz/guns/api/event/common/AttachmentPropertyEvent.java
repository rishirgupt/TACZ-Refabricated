package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;

public class AttachmentPropertyEvent extends BaseEvent implements KubeJSGunEventPoster<AttachmentPropertyEvent> {
   private final ItemStack gunItem;
   private final AttachmentCacheProperty cacheProperty;
   public static final Event<AttachmentPropertyEvent.Callback> CALLBACK = EventFactory.createArrayBacked(
      AttachmentPropertyEvent.Callback.class, callbacks -> event -> {
         for (AttachmentPropertyEvent.Callback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public AttachmentPropertyEvent(ItemStack gunItem, AttachmentCacheProperty attachmentProperty) {
      this.gunItem = gunItem;
      this.cacheProperty = attachmentProperty;
   }

   public ItemStack getGunItem() {
      return this.gunItem;
   }

   public AttachmentCacheProperty getCacheProperty() {
      return this.cacheProperty;
   }

   public interface Callback {
      void post(AttachmentPropertyEvent var1);
   }
}
