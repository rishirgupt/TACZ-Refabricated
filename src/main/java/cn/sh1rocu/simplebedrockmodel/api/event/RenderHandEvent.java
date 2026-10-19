package cn.sh1rocu.simplebedrockmodel.api.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.GunRender;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class RenderHandEvent extends BaseEvent implements ICancellableEvent {
   private final AbstractClientPlayer player;
   private final InteractionHand hand;
   private final ItemStack stack;
   private final PoseStack matrices;
   private final float tickDelta;
   private final float pitch;
   private final float swingProgress;
   private final float equipProgress;
   private final int light;
   public static final Event<RenderHandEvent.Callback> EVENT = EventFactory.createArrayBacked(RenderHandEvent.Callback.class, callbacks -> handEvent -> {
      for (RenderHandEvent.Callback callback : callbacks) {
         callback.post(handEvent);
      }
   });

   public RenderHandEvent(
      AbstractClientPlayer player,
      InteractionHand hand,
      ItemStack stack,
      PoseStack matrices,
      float tickDelta,
      float pitch,
      float swingProgress,
      float equipProgress,
      int light
   ) {
      this.player = player;
      this.hand = hand;
      this.stack = stack;
      this.matrices = matrices;
      this.tickDelta = tickDelta;
      this.pitch = pitch;
      this.swingProgress = swingProgress;
      this.equipProgress = equipProgress;
      this.light = light;
   }

   public AbstractClientPlayer getPlayer() {
      return this.player;
   }

   public ItemStack getItemStack() {
      return this.stack;
   }

   public PoseStack getPoseStack() {
      return this.matrices;
   }

   /** Anything drawn through this source is submitted when the hand render that fired this event finishes. */
   public MultiBufferSource getMultiBufferSource() {
      return GunRender.bufferSource();
   }

   public int getPackedLight() {
      return this.light;
   }

   public float getPartialTick() {
      return this.tickDelta;
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getEquipProgress() {
      return this.equipProgress;
   }

   public float getSwingProgress() {
      return this.swingProgress;
   }

   public interface Callback {
      void post(RenderHandEvent event);
   }
}
