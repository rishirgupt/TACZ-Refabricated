package com.tacz.guns.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.ItemDisplayContext;

public interface IFunctionalRenderer {
   void render(PoseStack var1, VertexConsumer var2, ItemDisplayContext var3, int var4, int var5);
}
