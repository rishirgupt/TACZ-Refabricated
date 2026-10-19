package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.api.extension.IBlockExtension;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
   @Inject(method = "onExplosionHit", at = @At("HEAD"), cancellable = true)
   private void tacz$onBlockExploded(
      BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> drops, CallbackInfo ci
   ) {
      if (state.getBlock() instanceof IBlockExtension block) {
         block.tacz$onBlockExploded(state, level, pos, explosion);
         ci.cancel();
      }
   }
}
