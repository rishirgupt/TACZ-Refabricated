package com.tacz.guns.entity.sync.core;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.HolderLookup.Provider;

public interface IDataSerializer<T> {
   void write(FriendlyByteBuf var1, T var2);

   T read(FriendlyByteBuf var1);

   Tag write(Provider var1, T var2);

   T read(Provider var1, Tag var2);
}
