package com.tacz.guns.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.Context;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IHandshakeMessage extends CustomPacketPayload {
   void handle(Context var1);

   interface IResponsePacket extends CustomPacketPayload {
      void handle(net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking.Context var1);
   }
}
