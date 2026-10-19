package cn.sh1rocu.tacz.api.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ComputeFovModifierEvent extends BaseEvent {
   private final Player player;
   private final float fovModifier;
   private float newFovModifier;
   public static final Event<ComputeFovModifierEvent.Callback> CALLBACK = EventFactory.createArrayBacked(
      ComputeFovModifierEvent.Callback.class, callbacks -> event -> {
         for (ComputeFovModifierEvent.Callback callback : callbacks) {
            callback.post(event);
         }
      }
   );

   public ComputeFovModifierEvent(Player player, float fovModifier) {
      this.player = player;
      this.fovModifier = fovModifier;
      this.setNewFovModifier((float)Mth.lerp((Double)Minecraft.getInstance().options.fovEffectScale().get(), 1.0, fovModifier));
   }

   public Player getPlayer() {
      return this.player;
   }

   public float getFovModifier() {
      return this.fovModifier;
   }

   public float getNewFovModifier() {
      return this.newFovModifier;
   }

   public void setNewFovModifier(float newFovModifier) {
      this.newFovModifier = newFovModifier;
   }

   public interface Callback {
      void post(ComputeFovModifierEvent var1);
   }
}
