package cn.sh1rocu.tacz.mixin.accessor;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockableEventLoop.class)
public interface BlockableEventLoopAccessor {
   @Invoker("submitAsync")
   CompletableFuture<Void> tacz$submitAsync(Runnable var1);
}
