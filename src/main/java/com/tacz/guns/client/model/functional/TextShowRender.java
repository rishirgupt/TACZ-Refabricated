package com.tacz.guns.client.model.functional;

import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.papi.PapiManager;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.LightCoordsUtil;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.gui.Font.DisplayMode;
import com.tacz.guns.client.render26.MultiBufferSource;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TextShowRender implements IFunctionalRenderer {
   private final BedrockModel bedrockModel;
   private final TextShow textShow;
   private final ItemStack gunStack;

   public TextShowRender(BedrockModel bedrockModel, TextShow textShow, ItemStack gunStack) {
      this.bedrockModel = bedrockModel;
      this.textShow = textShow;
      this.gunStack = gunStack;
   }

   @Override
   public void render(PoseStack poseStack, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
      if (transformType.firstPerson()) {
         String text = PapiManager.getTextShow(this.textShow.getTextKey(), this.gunStack);
         if (!StringUtils.isBlank(text)) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            Matrix3f normal = new Matrix3f(poseStack.last().normal());
            Matrix4f pose = new Matrix4f(poseStack.last().pose());
            this.bedrockModel
               .delegateRender(
                  (poseStack1, vertexBuffer1, transformType1, light1, overlay1) -> {
                     Font font = Minecraft.getInstance().font;
                     boolean shadow = this.textShow.isShadow();
                     int color = this.textShow.getColorInt();
                     float scale = this.textShow.getScale();
                     int packLight = LightCoordsUtil.pack(this.textShow.getTextLight(), this.textShow.getTextLight());
                     int width = font.width(text);

                     int xOffset = switch (this.textShow.getAlign()) {
                        case CENTER -> width / 2;
                        case RIGHT -> width;
                        default -> 0;
                     };
                     PoseStack poseStack2 = new PoseStack();
                     poseStack2.last().normal().mul(normal);
                     poseStack2.last().pose().mul(pose);
                     poseStack2.scale(0.006666667F * scale, -0.006666667F * scale, -0.006666667F);
                     net.minecraft.client.renderer.SubmitNodeCollector collector = com.tacz.guns.client.render26.GunRender.collector();
                     if (collector != null) {
                        collector.submitText(
                           poseStack2, -xOffset, -9 / 2.0F, net.minecraft.network.chat.Component.literal(text).getVisualOrderText(), shadow, DisplayMode.NORMAL, packLight, color, 0, 0
                        );
                     }
                  }
               );
         }
      }
   }
}
