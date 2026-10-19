package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.util.forge.network.IEntityExtension;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
   @Shadow
   @Final
   private Entity entity;

   @Inject(
      method = "sendPairingData",
      at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = Shift.AFTER, ordinal = 0)
   )
   private void sendComplexSpawnData(ServerPlayer serverPlayer, Consumer<Packet<?>> consumer, CallbackInfo ci) {
      if (this.entity instanceof IEntityExtension entityExtension) {
         entityExtension.tacz$sendPairingData(serverPlayer, customPacketPayload -> consumer.accept(new ClientboundCustomPayloadPacket(customPacketPayload)));
      }
   }
}
