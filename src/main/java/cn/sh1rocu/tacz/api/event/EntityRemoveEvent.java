package cn.sh1rocu.tacz.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;

public class EntityRemoveEvent extends BaseEvent {
   public static final Event<EntityRemoveEvent.Callback> EVENT = EventFactory.createArrayBacked(EntityRemoveEvent.Callback.class, callbacks -> event -> {
      for (EntityRemoveEvent.Callback e : callbacks) {
         e.onEntityRemove(event);
      }
   });
   private final Entity entity;

   public EntityRemoveEvent(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public interface Callback {
      void onEntityRemove(EntityRemoveEvent var1);
   }
}
