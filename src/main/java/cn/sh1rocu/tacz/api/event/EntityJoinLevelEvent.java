package cn.sh1rocu.tacz.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityJoinLevelEvent extends BaseEvent implements ICancellableEvent {
   private final Entity entity;
   private final Level level;
   private final boolean loadedFromDisk;
   public static final Event<EntityJoinLevelEvent.Callback> CALLBACK = EventFactory.createArrayBacked(
      EntityJoinLevelEvent.Callback.class, callbacks -> event -> {
         for (EntityJoinLevelEvent.Callback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public EntityJoinLevelEvent(Entity entity, Level level) {
      this(entity, level, false);
   }

   public EntityJoinLevelEvent(Entity entity, Level level, boolean loadedFromDisk) {
      this.entity = entity;
      this.level = level;
      this.loadedFromDisk = loadedFromDisk;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public Level getLevel() {
      return this.level;
   }

   public boolean loadedFromDisk() {
      return this.loadedFromDisk;
   }

   public interface Callback {
      void post(EntityJoinLevelEvent var1);
   }
}
