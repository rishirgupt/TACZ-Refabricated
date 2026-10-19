package cn.sh1rocu.tacz.mixin.client;

import com.tacz.guns.client.render26.EntityStateKeys;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
   @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
   private void tacz$postEvent(LivingEntityRenderState state, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera, CallbackInfo ci) {
      LivingEntity entity = state.getData(EntityStateKeys.ENTITY);
      if (entity != null) {
         ((cn.sh1rocu.tacz.api.event.RenderLivingEvent.PostCallback)cn.sh1rocu.tacz.api.event.RenderLivingEvent.POST.invoker()).post(new cn.sh1rocu.tacz.api.event.RenderLivingEvent.Post(entity, 1.0F, poseStack, collector, state.lightCoords));
      }
   }

   @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
   private void tacz$captureEntity(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
      state.setData(EntityStateKeys.ENTITY, entity);
   }
}
