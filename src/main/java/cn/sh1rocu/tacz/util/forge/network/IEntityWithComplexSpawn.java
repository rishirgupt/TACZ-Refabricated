package cn.sh1rocu.tacz.util.forge.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface IEntityWithComplexSpawn {
   void writeSpawnData(RegistryFriendlyByteBuf var1);

   void readSpawnData(RegistryFriendlyByteBuf var1);
}
