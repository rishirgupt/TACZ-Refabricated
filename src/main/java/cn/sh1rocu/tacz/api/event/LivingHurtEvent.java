package cn.sh1rocu.tacz.api.event;

import cn.sh1rocu.tacz.TaCZFabric;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;

public class LivingHurtEvent extends LivingEvent implements ICancellableEvent {
   private final DamageSource source;
   private float amount;
   public static final Event<LivingHurtEvent.Callback> CALLBACK = EventFactory.createWithPhases(LivingHurtEvent.Callback.class, callbacks -> event -> {
      for (LivingHurtEvent.Callback e : callbacks) {
         e.onLivingHurt(event);
      }
   }, new Identifier[]{TaCZFabric.HIGHEST, TaCZFabric.HIGH, Event.DEFAULT_PHASE, TaCZFabric.LOW, TaCZFabric.LOWEST});

   public LivingHurtEvent(LivingEntity entity, DamageSource source, float amount) {
      super(entity);
      this.source = source;
      this.amount = amount;
   }

   public DamageSource getSource() {
      return this.source;
   }

   public float getAmount() {
      return this.amount;
   }

   public void setAmount(float amount) {
      this.amount = amount;
   }

   public interface Callback {
      void onLivingHurt(LivingHurtEvent var1);
   }
}
