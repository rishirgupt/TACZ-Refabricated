package com.tacz.guns.client.render26;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/** Render types the gun models need that vanilla does not provide. */
public final class ModRenderTypes {
   /** Entity-style quads blended additively (laser beams, muzzle glows), not culled and not hidden by depth. */
   private static final RenderPipeline ADDITIVE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
      .withLocation("pipeline/tacz_entity_additive")
      .withShaderDefine("ALPHA_CUTOUT", 0.0F)
      .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
      .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
      .withCull(false)
      .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
      .build();
   /** Translucent entity-style quads that ignore depth: scope reticles drawn over the lens. */
   private static final RenderPipeline ALWAYS_ON_TOP = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
      .withLocation("pipeline/tacz_entity_always_on_top")
      .withShaderDefine("ALPHA_CUTOUT", 0.0F)
      .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final Map<Identifier, RenderType> ADDITIVE_TYPES = new ConcurrentHashMap<>();
   private static final Map<Identifier, RenderType> ON_TOP_TYPES = new ConcurrentHashMap<>();

   private ModRenderTypes() {
   }

   public static RenderType additive(Identifier texture) {
      return ADDITIVE_TYPES.computeIfAbsent(texture, id -> RenderType.create(
         "tacz_additive",
         RenderSetup.builder(ADDITIVE).withTexture("Sampler0", id).useLightmap().useOverlay().sortOnUpload().createRenderSetup()));
   }

   public static RenderType alwaysOnTop(Identifier texture) {
      return ON_TOP_TYPES.computeIfAbsent(texture, id -> RenderType.create(
         "tacz_on_top",
         RenderSetup.builder(ALWAYS_ON_TOP).withTexture("Sampler0", id).useLightmap().useOverlay().sortOnUpload().createRenderSetup()));
   }
}
