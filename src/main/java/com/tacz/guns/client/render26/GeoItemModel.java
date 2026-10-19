package com.tacz.guns.client.render26;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.client.renderer.item.AmmoItemRenderer;
import com.tacz.guns.client.renderer.item.AttachmentItemRenderer;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.client.renderer.item.GunSmithTableItemRenderer;
import java.util.function.Consumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

/**
 * Item model type {@code tacz:geo}: hands the item to one of the bedrock item renderers, with the display
 * context it is being drawn in. {@code kind} is one of gun, ammo, attachment, table.
 */
public record GeoItemModel(String kind) implements ItemModel, ItemModel.Unbaked {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "geo");
   public static final MapCodec<GeoItemModel> CODEC = RecordCodecBuilder.mapCodec(
      builder -> builder.group(com.mojang.serialization.Codec.STRING.fieldOf("kind").forGetter(GeoItemModel::kind)).apply(builder, GeoItemModel::new)
   );

   public static void register() {
      net.minecraft.client.renderer.item.ItemModels.ID_MAPPER.put(ID, CODEC);
      AmmoStatueProperty.register();
   }

   @Override
   public MapCodec<GeoItemModel> type() {
      return CODEC;
   }

   @Override
   public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
      return this;
   }

   @Override
   public void resolveDependencies(ResolvableModel.Resolver resolver) {
   }

   @Override
   public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
      state.appendModelIdentityElement(this);
      state.appendModelIdentityElement(stack.getItem());
      state.appendModelIdentityElement(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA));
      ItemStackRenderState.LayerRenderState layer = state.newLayer();
      layer.setExtents(ItemStackRenderState.LayerRenderState.NO_EXTENTS_SUPPLIER);
      layer.setupSpecialModel(new Renderer(this.kind, context), stack);
   }

   private record Renderer(String kind, ItemDisplayContext context) implements SpecialModelRenderer<ItemStack> {
      @Override
      public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outlineColor) {
         GunRender.begin(collector);
         try {
            MultiBufferSource buffer = GunRender.bufferSource();
            switch (this.kind) {
               case "gun" -> GunItemRendererWrapper.INSTANCE.get().renderByItem(stack, this.context, poseStack, buffer, light, overlay);
               case "ammo" -> AmmoItemRenderer.INSTANCE.get().renderByItem(stack, this.context, poseStack, buffer, light, overlay);
               case "attachment" -> AttachmentItemRenderer.INSTANCE.get().renderByItem(stack, this.context, poseStack, buffer, light, overlay);
               case "table" -> GunSmithTableItemRenderer.INSTANCE.get().renderByItem(stack, this.context, poseStack, buffer, light, overlay);
               default -> {
               }
            }
         } finally {
            GunRender.end();
         }
      }

      @Override
      public void getExtents(Consumer<Vector3fc> output) {
      }

      @Override
      public ItemStack extractArgument(ItemStack stack) {
         return stack;
      }
   }
}
