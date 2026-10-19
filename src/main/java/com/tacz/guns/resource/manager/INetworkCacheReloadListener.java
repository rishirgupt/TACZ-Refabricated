package com.tacz.guns.resource.manager;

import com.tacz.guns.resource.network.DataType;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;

public interface INetworkCacheReloadListener extends IdentifiableResourceReloadListener {
   Map<Identifier, String> getNetworkCache();

   DataType getType();
}
