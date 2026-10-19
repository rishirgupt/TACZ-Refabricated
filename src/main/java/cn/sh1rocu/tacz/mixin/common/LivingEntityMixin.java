package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.api.event.LivingHurtEvent;
import cn.sh1rocu.tacz.api.event.LivingKnockBackEvent;
import cn.sh1rocu.tacz.api.extension.IItem;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.tacz.guns.init.ModAttributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Shadow
   public abstract ItemStack getItemInHand(InteractionHand var1);

   @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
   private static Builder tacz$createLivingAttributes(Builder original) {
      return original.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ModAttributes.BULLET_RESISTANCE));
   }

   @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
   private void tacz$swingHand(InteractionHand hand, boolean bl, CallbackInfo ci) {
      ItemStack stack = this.getItemInHand(hand);
      if (!stack.isEmpty() && stack.getItem() instanceof IItem swing && swing.tacz$onEntitySwing(stack, (LivingEntity)(Object)this)) {
         ci.cancel();
      }
   }

   @WrapMethod(method = "actuallyHurt")
   private void tacz$actuallyHurt(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount, Operation<Void> original) {
      LivingHurtEvent event = new LivingHurtEvent((LivingEntity)(Object)this, source, amount);
      ((LivingHurtEvent.Callback)LivingHurtEvent.CALLBACK.invoker()).onLivingHurt(event);
      if (event.isCanceled() || event.getAmount() <= 0.0F) {
         return;
      }

      original.call(level, source, event.getAmount());
   }

   @WrapMethod(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V")
   private void tacz$knockback(double strength, double x, double z, DamageSource source, float f, boolean b, Operation<Void> original) {
      LivingKnockBackEvent event = new LivingKnockBackEvent((LivingEntity)(Object)this, (float)strength, x, z);
      ((LivingKnockBackEvent.Callback)LivingKnockBackEvent.CALLBACK.invoker()).onLivingKnockBack(event);
      if (event.isCanceled()) {
         return;
      }

      double newStrength = event.getOriginalStrength() != event.getStrength() ? event.getStrength() : strength;
      double newX = event.getOriginalRatioX() != event.getRatioX() ? event.getRatioX() : x;
      double newZ = event.getOriginalRatioZ() != event.getRatioZ() ? event.getRatioZ() : z;
      original.call(newStrength, newX, newZ, source, f, b);
   }
}
