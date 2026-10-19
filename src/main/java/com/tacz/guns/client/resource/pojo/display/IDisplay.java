package com.tacz.guns.client.resource.pojo.display;

import net.minecraft.resources.FileToIdConverter;

public interface IDisplay {
   FileToIdConverter converter = new FileToIdConverter("textures", ".png");

   void init();
}
