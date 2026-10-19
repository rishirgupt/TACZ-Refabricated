package com.tacz.guns.client.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.functional.AttachmentRender;
import com.tacz.guns.client.model.functional.BeamRenderer;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.model.functional.TextShowRender;
import com.tacz.guns.client.model.listener.model.ModelAdditionalMagazineListener;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.compat.ar.ARCompat;
import com.tacz.guns.util.RenderHelper;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

public class BedrockGunModel extends BedrockAnimatedModel {
   protected final EnumMap<AttachmentType, List<BedrockPart>> refitAttachmentViewPath = Maps.newEnumMap(AttachmentType.class);
   private final EnumMap<AttachmentType, ItemStack> currentAttachmentItem = Maps.newEnumMap(AttachmentType.class);
   private final Set<String> adapterToRender = Sets.newHashSet();
   private final ArrayList<ShellRender> shellRenderList = new ArrayList<>();
   @Nullable
   protected List<BedrockPart> ironSightPath;
   @Nullable
   protected List<BedrockPart> idleSightPath;
   @Nullable
   protected List<BedrockPart> thirdPersonHandOriginPath;
   @Nullable
   protected List<BedrockPart> fixedOriginPath;
   @Nullable
   protected List<BedrockPart> groundOriginPath;
   @Nullable
   protected List<BedrockPart> scopePosPath;
   @Nullable
   protected List<BedrockPart> muzzleFlashPosPath;
   @Nullable
   protected BedrockPart root;
   @Nullable
   protected BedrockPart magazineNode;
   @Nullable
   protected BedrockPart additionalMagazineNode;
   @Nullable
   protected List<BedrockPart> laserBeamPaths;
   private boolean renderHand = true;
   private boolean renderMount;
   private ItemStack currentGunItem;
   private int currentExtendMagLevel = 0;

   public BedrockGunModel(BedrockModelPOJO pojo, BedrockVersion version) {
      super(pojo, version);
      this.magazineNode = Optional.ofNullable(this.modelMap.get("magazine")).map(ModelRendererWrapper::getModelRenderer).orElse(null);
      this.additionalMagazineNode = Optional.ofNullable(this.modelMap.get("additional_magazine")).map(ModelRendererWrapper::getModelRenderer).orElse(null);
      this.setFunctionalRenderer("lefthand_pos", bedrockPart -> new LeftHandRender(this));
      this.setFunctionalRenderer("righthand_pos", bedrockPart -> new RightHandRender(this));
      this.setFunctionalRenderer("muzzle_flash", bedrockPart -> new MuzzleFlashRender(this));
      this.setFunctionalRenderer("bullet_in_barrel", bedrockPart -> this.ammoHiddenRender(bedrockPart, iGun -> iGun.hasBulletInBarrel(this.currentGunItem)));
      this.setFunctionalRenderer("bullet_in_mag", bedrockPart -> this.ammoHiddenRender(bedrockPart, iGun -> iGun.getCurrentAmmoCount(this.currentGunItem) > 0));
      this.setFunctionalRenderer("bullet_chain", bedrockPart -> this.ammoHiddenRender(bedrockPart, iGun -> iGun.getCurrentAmmoCount(this.currentGunItem) > 0));
      this.setFunctionalRenderer(
         "mount", bedrockPart -> this.scopeHiddenRender(bedrockPart, scopeItem -> scopeItem != null && !scopeItem.isEmpty() && this.renderMount)
      );
      this.setFunctionalRenderer("carry", bedrockPart -> this.scopeHiddenRender(bedrockPart, scopeItem -> scopeItem == null || scopeItem.isEmpty()));
      this.setFunctionalRenderer("sight_folded", bedrockPart -> this.scopeHiddenRender(bedrockPart, scopeItem -> scopeItem != null && !scopeItem.isEmpty()));
      this.setFunctionalRenderer("sight", bedrockPart -> this.scopeHiddenRender(bedrockPart, scopeItem -> scopeItem == null || scopeItem.isEmpty()));
      this.setFunctionalRenderer("mag_extended_1", bedrockPart -> this.extendedMagHiddenRender(bedrockPart, 1));
      this.setFunctionalRenderer("mag_extended_2", bedrockPart -> this.extendedMagHiddenRender(bedrockPart, 2));
      this.setFunctionalRenderer("mag_extended_3", bedrockPart -> this.extendedMagHiddenRender(bedrockPart, 3));
      this.setFunctionalRenderer("mag_standard", bedrockPart -> this.extendedMagHiddenRender(bedrockPart, 0));
      this.setFunctionalRenderer("additional_magazine", this::renderAdditionalMagazine);
      this.setFunctionalRenderer("handguard_default", this::handguardDefaultRender);
      this.setFunctionalRenderer("handguard_tactical", this::handguardTacticalRender);
      this.cacheOtherPath();
      this.cacheRefitAttachmentViewPath();
      this.cacheShellOriginNodes();
      this.allAttachmentRender();
      this.setFunctionalRenderer("attachment_adapter", this::attachmentAdapterNodeRender);
   }

   private void cacheOtherPath() {
      this.ironSightPath = this.getPath(this.modelMap.get("iron_view"));
      this.idleSightPath = this.getPath(this.modelMap.get("idle_view"));
      this.thirdPersonHandOriginPath = this.getPath(this.modelMap.get("thirdperson_hand"));
      this.fixedOriginPath = this.getPath(this.modelMap.get("fixed"));
      this.groundOriginPath = this.getPath(this.modelMap.get("ground"));
      this.muzzleFlashPosPath = this.getPath(this.modelMap.get("muzzle_flash"));
      this.scopePosPath = this.getPath(this.modelMap.get(AttachmentType.SCOPE.name().toLowerCase() + "_pos"));
      this.laserBeamPaths = this.getPath(this.modelMap.get("laser_beam"));
      this.root = Optional.ofNullable(this.modelMap.get("root")).map(ModelRendererWrapper::getModelRenderer).orElse(null);
   }

   private void cacheRefitAttachmentViewPath() {
      for (AttachmentType type : AttachmentType.values()) {
         if (type == AttachmentType.NONE) {
            this.refitAttachmentViewPath.put(type, this.getPath(this.modelMap.get("refit_view")));
         } else {
            String nodeName = "refit_" + type.name().toLowerCase() + "_view";
            this.refitAttachmentViewPath.put(type, this.getPath(this.modelMap.get(nodeName)));
         }
      }
   }

   private void cacheShellOriginNodes() {
      ModelRendererWrapper rendererWrapper = this.modelMap.get("shell");

      for (int i = 1; rendererWrapper != null; i++) {
         ShellRender shellRender = new ShellRender(this);
         this.setFunctionalRenderer(rendererWrapper.getModelRenderer().name, bedrockPart -> shellRender);
         this.shellRenderList.add(shellRender);
         rendererWrapper = this.modelMap.get("shell_" + i);
      }
   }

   @Nullable
   private IFunctionalRenderer attachmentAdapterNodeRender(BedrockPart bedrockPart) {
      ObjectListIterator var2 = bedrockPart.children.iterator();

      while (var2.hasNext()) {
         BedrockPart child = (BedrockPart)var2.next();
         if (child.name == null) {
            child.visible = false;
         } else {
            child.visible = this.adapterToRender.contains(child.name);
         }
      }

      return null;
   }

   private void allAttachmentRender() {
      for (AttachmentType type : AttachmentType.values()) {
         if (type != AttachmentType.NONE && type != AttachmentType.SCOPE) {
            String positionNodeName = type.name().toLowerCase() + "_pos";
            String defaultNodeName = type.name().toLowerCase() + "_default";
            this.setFunctionalRenderer(positionNodeName, bedrockPart -> {
               bedrockPart.visible = false;
               return new AttachmentRender(this, type);
            });
            this.setFunctionalRenderer(defaultNodeName, bedrockPart -> {
               ItemStack attachmentItem = this.currentAttachmentItem.get(type);
               if (type == AttachmentType.MUZZLE && checkShowMuzzle(bedrockPart, attachmentItem)) {
                  return null;
               }

               bedrockPart.visible = attachmentItem == null || attachmentItem.isEmpty();
               return null;
            });
         }
      }
   }

   private static boolean checkShowMuzzle(BedrockPart bedrockPart, ItemStack attachmentItem) {
      IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
      if (iAttachment != null) {
         Identifier attachmentId = iAttachment.getAttachmentId(attachmentItem);
         Optional<ClientAttachmentIndex> attachmentIndex = TimelessAPI.getClientAttachmentIndex(attachmentId);
         if (attachmentIndex.isPresent()) {
            bedrockPart.visible = attachmentIndex.get().isShowMuzzle();
            return true;
         }
      }

      return false;
   }

   @Nullable
   private IFunctionalRenderer handguardTacticalRender(BedrockPart bedrockPart) {
      ItemStack laserItem = this.currentAttachmentItem.get(AttachmentType.LASER);
      ItemStack gripItem = this.currentAttachmentItem.get(AttachmentType.GRIP);
      bedrockPart.visible = !laserItem.isEmpty() || !gripItem.isEmpty();
      return null;
   }

   @Nullable
   private IFunctionalRenderer handguardDefaultRender(BedrockPart bedrockPart) {
      ItemStack laserItem = this.currentAttachmentItem.get(AttachmentType.LASER);
      ItemStack gripItem = this.currentAttachmentItem.get(AttachmentType.GRIP);
      bedrockPart.visible = laserItem.isEmpty() && gripItem.isEmpty();
      return null;
   }

   @NotNull
   private IFunctionalRenderer renderAdditionalMagazine(BedrockPart bedrockPart) {
      return (poseStack, vertexBuffer, transformType, light, overlay) -> {
         if (bedrockPart.visible) {
            bedrockPart.compile(poseStack.last(), vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            ObjectListIterator var7 = bedrockPart.children.iterator();

            while (var7.hasNext()) {
               BedrockPart part = (BedrockPart)var7.next();
               part.render(poseStack, transformType, vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            }

            if (this.magazineNode != null && this.magazineNode.visible) {
               this.magazineNode.compile(poseStack.last(), vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
               var7 = this.magazineNode.children.iterator();

               while (var7.hasNext()) {
                  BedrockPart part = (BedrockPart)var7.next();
                  part.render(poseStack, transformType, vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }
      };
   }

   public void setTextShowList(Map<String, TextShow> textShowList) {
      textShowList.forEach((name, textShow) -> this.setFunctionalRenderer(name, bedrockPart -> new TextShowRender(this, textShow, this.currentGunItem)));
   }

   public void render(PoseStack matrixStack, ItemStack gunItem, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun != null) {
         this.currentGunItem = gunItem;
         this.currentExtendMagLevel = 0;
         this.adapterToRender.clear();

         for (AttachmentType type : AttachmentType.values()) {
            if (type != AttachmentType.NONE) {
               ItemStack attachmentItem = iGun.getAttachment(Minecraft.getInstance().level.registryAccess(), gunItem, type);
               if (attachmentItem.isEmpty()) {
                  attachmentItem = iGun.getBuiltinAttachment(gunItem, type);
               }

               this.currentAttachmentItem.put(type, attachmentItem);
               IAttachment attachment = IAttachment.getIAttachmentOrNull(attachmentItem);
               if (attachment != null) {
                  TimelessAPI.getClientAttachmentIndex(attachment.getAttachmentId(attachmentItem)).ifPresent(index -> {
                     if (type == AttachmentType.EXTENDED_MAG) {
                        this.currentExtendMagLevel = index.getData().getExtendedMagLevel();
                     }

                     if (type == AttachmentType.SCOPE) {
                        this.renderMount = index.isShowMount();
                     }

                     if (index.getAdapterNodeName() != null) {
                        this.adapterToRender.add(index.getAdapterNodeName());
                     }
                  });
               }
            }
         }

         if (this.laserBeamPaths != null) {
            BeamRenderer.renderLaserBeam(gunItem, matrixStack, transformType, this.laserBeamPaths);
         }

         ItemStack attachmentItem = this.currentAttachmentItem.get(AttachmentType.SCOPE);
         if (this.scopePosPath != null && attachmentItem != null && !attachmentItem.isEmpty()) {
            matrixStack.pushPose();

            for (BedrockPart bedrockPart : this.scopePosPath) {
               bedrockPart.translateAndRotateAndScale(matrixStack);
            }

            AttachmentRender.renderAttachment(attachmentItem, this.currentGunItem, matrixStack, transformType, light, overlay);
            matrixStack.popPose();
         }

         super.render(matrixStack, transformType, renderType, light, overlay);
      }
   }

   @Nullable
   private IFunctionalRenderer ammoHiddenRender(BedrockPart bedrockPart, Predicate<IGun> predicate) {
      IGun iGun = IGun.getIGunOrNull(this.currentGunItem);
      if (iGun != null) {
         bedrockPart.visible = predicate.test(iGun);
      }

      return null;
   }

   @Nullable
   private IFunctionalRenderer scopeHiddenRender(BedrockPart bedrockPart, Predicate<ItemStack> predicate) {
      ItemStack scopeItem = this.currentAttachmentItem.get(AttachmentType.SCOPE);
      bedrockPart.visible = predicate.test(scopeItem);
      return null;
   }

   @Nullable
   private IFunctionalRenderer extendedMagHiddenRender(BedrockPart bedrockPart, int level) {
      bedrockPart.visible = this.currentExtendMagLevel == level;
      return null;
   }

   @Override
   public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
      AnimationListener listener = super.supplyListeners(nodeName, type);
      if (listener == null) {
         return null;
      } else {
         return nodeName.equals("additional_magazine") ? new ModelAdditionalMagazineListener(listener, this) : listener;
      }
   }

   @Override
   public void cleanAnimationTransform() {
      super.cleanAnimationTransform();
      if (this.additionalMagazineNode != null) {
         this.additionalMagazineNode.visible = false;
      }
   }

   public EnumMap<AttachmentType, ItemStack> getCurrentAttachmentItem() {
      return this.currentAttachmentItem;
   }

   public ItemStack getCurrentGunItem() {
      return this.currentGunItem;
   }

   @Nullable
   public BedrockPart getAdditionalMagazineNode() {
      return this.additionalMagazineNode;
   }

   @Nullable
   public List<BedrockPart> getIronSightPath() {
      return this.ironSightPath;
   }

   @Nullable
   @Override
   public List<BedrockPart> getIdleSightPath() {
      return this.idleSightPath;
   }

   @Nullable
   public List<BedrockPart> getThirdPersonHandOriginPath() {
      return this.thirdPersonHandOriginPath;
   }

   @Nullable
   public List<BedrockPart> getFixedOriginPath() {
      return this.fixedOriginPath;
   }

   @Nullable
   public List<BedrockPart> getGroundOriginPath() {
      return this.groundOriginPath;
   }

   @Nullable
   public List<BedrockPart> getMuzzleFlashPosPath() {
      return this.muzzleFlashPosPath;
   }

   @Nullable
   public List<BedrockPart> getScopePosPath() {
      return this.scopePosPath;
   }

   @Nullable
   public List<BedrockPart> getRefitAttachmentViewPath(AttachmentType type) {
      return this.refitAttachmentViewPath.get(type);
   }

   @Nullable
   public ShellRender getShellRender(int index) {
      return index >= 0 && index < this.shellRenderList.size() ? this.shellRenderList.get(index) : null;
   }

   @Nullable
   @Override
   public BedrockPart getRootNode() {
      return this.root;
   }

   @Override
   public boolean getRenderHand() {
      return this.renderHand;
   }

   public void setRenderHand(boolean renderHand) {
      this.renderHand = renderHand;
   }
}
