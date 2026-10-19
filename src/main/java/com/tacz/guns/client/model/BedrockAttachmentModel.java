package com.tacz.guns.client.model;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.functional.BeamRenderer;
import com.tacz.guns.client.model.functional.TextShowRender;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemDisplayContext;
import com.tacz.guns.client.render26.MultiBufferSource;
import org.joml.Vector3f;

public class BedrockAttachmentModel extends BedrockAnimatedModel {
   private static final String SCOPE_VIEW_NODE = "scope_view";
   private static final String SCOPE_BODY_NODE = "scope_body";
   private static final String OCULAR_RING_NODE = "ocular_ring";
   private static final String DIVISION_NODE = "division";
   private static final String OCULAR_NODE = "ocular";
   private static final String OCULAR_SIGHT_NODE = "ocular_sight";
   private static final String OCULAR_SCOPE_NODE = "ocular_scope";
   private static final Pattern LASER_BEAM_PATTERN = Pattern.compile("^laser_beam(_(\\d+))?$");
   protected List<List<BedrockPart>> scopeViewPaths;
   @Nullable
   protected List<BedrockPart> scopeBodyPath;
   @Nullable
   protected List<BedrockPart> ocularRingPath;
   protected List<List<BedrockPart>> ocularNodePaths;
   protected List<Boolean> isScopeOcular;
   protected List<List<BedrockPart>> divisionNodePaths;
   @Nullable
   protected List<List<BedrockPart>> laserBeamPaths;
   @Nullable
   private ItemStack currentGunItem;
   @Nullable
   private ItemStack attachmentItem;
   @Nullable
   private net.minecraft.resources.Identifier reticleTexture;
   private boolean isScope = false;
   private boolean isSight = false;
   private float scopeViewRadiusModifier = 1.0F;

   public BedrockAttachmentModel(BedrockModelPOJO pojo, BedrockVersion version) {
      super(pojo, version);
      this.scopeViewPaths = new ArrayList<>();
      this.ocularNodePaths = new ArrayList<>();
      this.isScopeOcular = new ArrayList<>();
      this.divisionNodePaths = new ArrayList<>();
      this.laserBeamPaths = new ArrayList<>();
      List<BedrockPart> path = this.getPath(this.modelMap.get("scope_view"));

      for (int i = 2; path != null; path = this.getPath(this.modelMap.get("scope_view_" + i++))) {
         this.scopeViewPaths.add(path);
      }

      String ocularRegex = "^(ocular|ocular_sight|ocular_scope)(_(\\d+))?$";
      Pattern ocularPattern = Pattern.compile(ocularRegex);
      TreeMap<Integer, BedrockAttachmentModel.OcularWrapper> map = new TreeMap<>();

      for (Entry<String, ModelRendererWrapper> entry : this.modelMap.entrySet()) {
         Matcher matcher = ocularPattern.matcher(entry.getKey());
         if (matcher.matches()) {
            int num = 1;
            String numStr = matcher.group(3);
            if (numStr != null) {
               num = Integer.parseInt(numStr);
            }

            String type = matcher.group(1);
            boolean isScope = "ocular_scope".equals(type);
            map.put(num, new BedrockAttachmentModel.OcularWrapper(entry.getValue(), isScope));
         }

         if (LASER_BEAM_PATTERN.matcher(entry.getKey()).find()) {
            this.laserBeamPaths.add(this.getPath(entry.getValue()));
         }
      }

      for (BedrockAttachmentModel.OcularWrapper wrapper : map.values()) {
         this.ocularNodePaths.add(this.getPath(wrapper.renderer));
         this.isScopeOcular.add(wrapper.isScope);
      }

      ModelRendererWrapper divisionModel = this.modelMap.get("division");
      path = this.getPath(this.modelMap.get("division"));
      int var16 = 2;

      while (path != null) {
         this.divisionNodePaths.add(path);
         divisionModel.setHidden(true);
         divisionModel = this.modelMap.get("division_" + var16++);
         path = this.getPath(divisionModel);
      }

      this.scopeBodyPath = this.getPath(this.modelMap.get("scope_body"));
      this.ocularRingPath = this.getPath(this.modelMap.get("ocular_ring"));
   }

   @Nullable
   public List<BedrockPart> getScopeViewPath(int viewSwitchCount) {
      if (this.scopeViewPaths.isEmpty()) {
         return null;
      } else {
         return viewSwitchCount >= this.scopeViewPaths.size() ? this.scopeViewPaths.get(0) : this.scopeViewPaths.get(viewSwitchCount);
      }
   }

   public void setReticleTexture(@Nullable net.minecraft.resources.Identifier texture) {
      this.reticleTexture = texture;
   }

   public void setIsScope(boolean isScope) {
      this.isScope = isScope;
   }

   public void setIsSight(boolean isSight) {
      this.isSight = isSight;
   }

   public boolean isScope() {
      return this.isScope;
   }

   public boolean isSight() {
      return this.isSight;
   }

   @Nullable
   public net.minecraft.resources.Identifier getReticleTexture() {
      return this.reticleTexture;
   }

   public void setScopeViewRadiusModifier(float scopeViewRadiusModifier) {
      this.scopeViewRadiusModifier = scopeViewRadiusModifier;
   }

   public void setTextShowList(Map<String, TextShow> textShowList) {
      textShowList.forEach((name, textShow) -> this.setFunctionalRenderer(name, bedrockPart -> new TextShowRender(this, textShow, this.currentGunItem)));
   }

   public void render(
      @Nullable ItemStack attachmentItem,
      ItemStack currentGunItem,
      PoseStack matrixStack,
      ItemDisplayContext transformType,
      RenderType renderType,
      int light,
      int overlay
   ) {
      this.currentGunItem = currentGunItem;
      this.attachmentItem = attachmentItem;
      if (transformType.firstPerson()) {
         if (this.isScope) {
            // The lens is a magnified crop of the finished frame drawn on the HUD (see ScopeLens) inside the ocular outline;
            // only that outline is measured here. The scope model itself is drawn by the shared call below.
            this.captureLens(matrixStack, transformType, renderType, light, overlay);
         } else if (this.isSight) {
            this.renderSight(matrixStack, transformType, renderType, light, overlay);
         }
      } else {
         if (this.scopeBodyPath != null && isLookingThroughScope()) {
            this.renderTempPart(matrixStack, transformType, renderType, light, overlay, this.scopeBodyPath);
         }

         if (this.ocularRingPath != null && isLookingThroughScope()) {
            this.renderTempPart(matrixStack, transformType, renderType, light, overlay, this.ocularRingPath);
         }
      }

      if (!this.isScope && !this.isSight && this.laserBeamPaths != null) {
         for (List<BedrockPart> entry : this.laserBeamPaths) {
            BeamRenderer.renderLaserBeam(attachmentItem, matrixStack, transformType, entry);
         }
      }

      super.render(matrixStack, transformType, renderType, light, overlay);
      if ((this.isScope || this.isSight) && this.laserBeamPaths != null) {
         for (List<BedrockPart> entry : this.laserBeamPaths) {
            BeamRenderer.renderLaserBeam(attachmentItem, matrixStack, transformType, entry);
         }
      }
   }

   private void captureLens(PoseStack poseStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
      com.tacz.guns.client.gui.overlay.ScopeLens.abandon();
      if (this.ocularNodePaths.isEmpty()) {
         return;
      }

      VertexConsumer sink = com.tacz.guns.client.gui.overlay.ScopeLens.pointSink();
      // Dual-mode scopes carry a magnifier lens (ocular_scope) and a red dot lens (ocular_sight); hip view is the magnifier.
      boolean anyScopeOcular = this.isScopeOcular.contains(true);
      for (int index = 0; index < this.ocularNodePaths.size(); index++) {
         if (anyScopeOcular && !this.isScopeOcular.get(index)) {
            continue;
         }

         List<BedrockPart> path = this.ocularNodePaths.get(index);
         com.tacz.guns.client.gui.overlay.ScopeLens.startPath();
         poseStack.pushPose();
         for (int i = 0; i < path.size() - 1; i++) {
            path.get(i).translateAndRotateAndScale(poseStack);
         }

         BedrockPart part = path.get(path.size() - 1);
         boolean visible = part.visible;
         part.visible = true;
         part.render(poseStack, transformType, sink, light, overlay);
         part.visible = visible;
         poseStack.popPose();
      }

      org.joml.Vector3f forward = poseStack.last().pose().transformDirection(0.0F, 0.0F, -1.0F, new org.joml.Vector3f());
      com.tacz.guns.client.gui.overlay.ScopeLens.commit(forward.x, forward.y, forward.z);
   }

   private void renderTempPart(PoseStack poseStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, @Nonnull List<BedrockPart> path) {
      poseStack.pushPose();

      for (int i = 0; i < path.size() - 1; i++) {
         path.get(i).translateAndRotateAndScale(poseStack);
      }

      BedrockPart part = path.get(path.size() - 1);
      part.visible = true;
      VertexConsumer vertexConsumer = com.tacz.guns.client.render26.GunRender.bufferSource().getBuffer(renderType);
      part.render(poseStack, transformType, vertexConsumer, light, overlay);
      part.visible = false;
      poseStack.popPose();
   }

   /**
    * 26.2 has no stencil buffer, so a reticle cannot be clipped to the lens. It is instead drawn over the lens
    * without a depth test, which looks the same for the usual reticle sizes.
    */
   private void renderDivisions(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, boolean selective) {
      if (this.isScope && !isLookingThroughScope()) {
         return;
      }

      RenderType divisionType = this.reticleTexture != null ? com.tacz.guns.client.render26.ModRenderTypes.alwaysOnTop(this.reticleTexture) : renderType;

      for (int i = 0; i < this.divisionNodePaths.size(); i++) {
         if (!selective || i >= this.isScopeOcular.size() || !this.isScopeOcular.get(i)) {
            this.renderTempPart(matrixStack, transformType, divisionType, light, overlay, this.divisionNodePaths.get(i));
         }
      }
   }

   /** The ocular is the opaque frame around the lens; without a stencil mask it is only drawn while looking through the scope. */
   private static boolean isLookingThroughScope() {
      net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
      return player instanceof com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator operator && operator.getClientAimingProgress(1.0F) > 0.85F;
   }

   private void renderOculars(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
      if (!isLookingThroughScope()) {
         return;
      }

      for (List<BedrockPart> path : this.ocularNodePaths) {
         this.renderTempPart(matrixStack, transformType, renderType, light, overlay, path);
      }
   }

   private void renderBoth(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
      if (this.ocularRingPath != null && isLookingThroughScope()) {
         this.renderTempPart(matrixStack, transformType, renderType, light, overlay, this.ocularRingPath);
      }

      if (this.scopeBodyPath != null && isLookingThroughScope()) {
         this.renderTempPart(matrixStack, transformType, renderType, light, overlay, this.scopeBodyPath);
      }

      this.renderOculars(matrixStack, transformType, renderType, light, overlay);
      this.renderDivisions(matrixStack, transformType, renderType, light, overlay, false);
      super.render(matrixStack, transformType, renderType, light, overlay);
   }

   private void renderSight(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
      this.renderOculars(matrixStack, transformType, renderType, light, overlay);
      this.renderDivisions(matrixStack, transformType, renderType, light, overlay, false);
      if (this.scopeBodyPath != null && isLookingThroughScope()) {
         this.renderTempPart(matrixStack, transformType, renderType, light, overlay, this.scopeBodyPath);
      }

      super.render(matrixStack, transformType, renderType, light, overlay);
   }

   private void renderScope(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
      if (this.ocularRingPath != null && isLookingThroughScope()) {
         this.renderTempPart(matrixStack, transformType, renderType, light, overlay, this.ocularRingPath);
      }

      if (this.scopeBodyPath != null && isLookingThroughScope()) {
         this.renderTempPart(matrixStack, transformType, renderType, light, overlay, this.scopeBodyPath);
      }

      this.renderOculars(matrixStack, transformType, renderType, light, overlay);
      this.renderDivisions(matrixStack, transformType, renderType, light, overlay, false);
      super.render(matrixStack, transformType, renderType, light, overlay);
   }

   private static class OcularWrapper {
      public ModelRendererWrapper renderer;
      public boolean isScope;

      public OcularWrapper(ModelRendererWrapper renderer, boolean isScope) {
         this.renderer = renderer;
         this.isScope = isScope;
      }
   }
}
