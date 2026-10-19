package com.tacz.guns.network;

import com.tacz.guns.network.message.handshake.SyncedEntityDataMappingS2CPacket;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask.Type;
import org.jetbrains.annotations.NotNull;

public class HandshakeNetworking {
   public record SyncedEntityDataTask(FabricServerConfigurationPacketListenerImpl handler) implements ConfigurationTask {
      public static final Type TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "synced_entity_data_mapping").toString());

      public void start(@NotNull Consumer<Packet<?>> task) {
         this.run(customPacketPayload -> task.accept(new ClientboundCustomPayloadPacket(customPacketPayload)));
      }

      @NotNull
      public Type type() {
         return TYPE;
      }

      public void run(@NotNull Consumer<CustomPacketPayload> consumer) {
         consumer.accept(new SyncedEntityDataMappingS2CPacket());
         this.handler.completeTask(this.type());
      }
   }
}
