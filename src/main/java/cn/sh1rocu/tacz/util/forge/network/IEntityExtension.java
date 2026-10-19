package cn.sh1rocu.tacz.util.forge.network;

import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IEntityExtension {
   default void tacz$sendPairingData(ServerPlayer serverPlayer, Consumer<CustomPacketPayload> bundleBuilder) {
      if (this instanceof IEntityWithComplexSpawn) {
         bundleBuilder.accept(new AdvancedAddEntityPayload((Entity)this));
      }
   }
}
