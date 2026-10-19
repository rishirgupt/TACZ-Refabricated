package com.tacz.guns.api.event.server;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import com.tacz.guns.api.event.common.KubeJSGunEventPoster;
import com.tacz.guns.entity.EntityKineticBullet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmmoHitBlockEvent extends BaseEvent implements KubeJSGunEventPoster<AmmoHitBlockEvent>, ICancellableEvent {
   private final Level level;
   private final BlockHitResult hitResult;
   private final BlockState state;
   private final EntityKineticBullet ammo;
   public static final Event<AmmoHitBlockEvent.Callback> CALLBACK = EventFactory.createArrayBacked(AmmoHitBlockEvent.Callback.class, callbacks -> event -> {
      for (AmmoHitBlockEvent.Callback e : callbacks) {
         e.post(event);
      }
   });

   public AmmoHitBlockEvent(Level level, BlockHitResult hitResult, BlockState state, EntityKineticBullet ammo) {
      this.level = level;
      this.hitResult = hitResult;
      this.state = state;
      this.ammo = ammo;
      this.postServerEventToKubeJS(this);
   }

   public Level getLevel() {
      return this.level;
   }

   public BlockHitResult getHitResult() {
      return this.hitResult;
   }

   public BlockState getState() {
      return this.state;
   }

   public EntityKineticBullet getAmmo() {
      return this.ammo;
   }

   public interface Callback {
      void post(AmmoHitBlockEvent var1);
   }
}
