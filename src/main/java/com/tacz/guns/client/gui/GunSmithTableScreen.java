package com.tacz.guns.client.gui;

import cn.sh1rocu.tacz.mixin.accessor.ScreenAccessor;
import cn.sh1rocu.tacz.util.forge.ImageButton;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.components.FlatColorButton;
import com.tacz.guns.client.gui.components.GunPackList;
import com.tacz.guns.client.gui.components.smith.ResultButton;
import com.tacz.guns.client.gui.components.smith.TypeButton;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.inventory.GunSmithTableMenu;
import com.tacz.guns.network.message.ClientMessageCraft;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.tacz.guns.util.RenderDistance;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.tacz.guns.client.render26.MultiBufferSource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class GunSmithTableScreen extends AbstractContainerScreen<GunSmithTableMenu> {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/gun_smith_table.png");
   private static final Identifier SIDE = Identifier.fromNamespaceAndPath("tacz", "textures/gui/gun_smith_table_side.png");
   private final LinkedHashMap<Identifier, TabConfig> recipeKeys = Maps.newLinkedHashMap();
   private final Map<Identifier, List<Identifier>> recipes = Maps.newLinkedHashMap();
   private int typePage;
   private Identifier selectedType = null;
   private List<Identifier> selectedRecipeList = new ArrayList<>();
   private int indexPage;
   @Nullable
   private com.tacz.guns.client.resource.ClientRecipeCache.Entry selectedRecipe;
   @Nullable
   private Int2IntArrayMap playerIngredientCount;
   private int scale = 70;
   private boolean filterEnabled = false;
   private GunPackList filterList;
   private boolean autoByHandFilterApplied = false;

   public GunSmithTableScreen(GunSmithTableMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 344, 186);
      this.classifyRecipes();
      this.typePage = 0;
      this.indexPage = 0;
      this.selectedRecipe = this.getSelectedRecipe(
         this.selectedRecipeList != null && !this.selectedRecipeList.isEmpty() ? this.selectedRecipeList.get(0) : null
      );
      this.getPlayerIngredientCount(this.selectedRecipe);
   }

   public static void drawModCenteredString(GuiGraphicsExtractor gui, Font font, Component component, int pX, int pY, int color) {
      FormattedCharSequence text = component.getVisualOrderText();
      gui.text(font, text, pX - font.width(text) / 2, pY, color, false);
   }

   private void classifyRecipes() {
      this.recipes.clear();
      this.recipeKeys.clear();
      Identifier blockId = ((GunSmithTableMenu)this.menu).getBlockId();
      if (blockId != null) {
         Map<Identifier, List<Identifier>> recipes = Maps.newLinkedHashMap();
         Map<Identifier, TabConfig> recipeKeys = Maps.newLinkedHashMap();
         TimelessAPI.getCommonBlockIndex(blockId).ifPresent(blockIndex -> {
            List<TabConfig> tabs = blockIndex.getData().getTabs();
            if (DefaultAssets.DEFAULT_BLOCK_ID.equals(blockId) && !(Boolean)SyncConfig.ENABLE_TABLE_FILTER.get()) {
               tabs = TabConfig.defaultTabs();
            }

            for (TabConfig tab : tabs) {
               recipes.put(tab.id(), Lists.newArrayList());
               recipeKeys.put(tab.id(), tab);
            }
         });
         List<Pair<Identifier, Identifier>> recipeIds = Lists.newArrayList();
         if (Minecraft.getInstance().level != null) {
            List<com.tacz.guns.client.resource.ClientRecipeCache.Entry> recipeList = com.tacz.guns.client.resource.ClientRecipeCache.entries();
            Set<String> namespaces = this.filterList != null ? this.filterList.namespaceList() : null;

            for (com.tacz.guns.client.resource.ClientRecipeCache.Entry holder : recipeList) {
               GunSmithTableRecipe recipe = (GunSmithTableRecipe)holder.value();
               Identifier id = holder.id();
               if ((namespaces == null || namespaces.contains(id.getNamespace())) && this.isSuitableForMainHand(recipe) && this.isNameMatch(recipe)) {
                  Identifier groupName = recipe.getResult().getGroup();
                  if (recipeKeys.containsKey(groupName)) {
                     recipeIds.add(Pair.of(groupName, id));
                  }
               }
            }
         }

         TimelessAPI.getCommonBlockIndex(((GunSmithTableMenu)this.menu).getBlockId()).map(blockIndex -> {
            if (((GunSmithTableMenu)this.menu).getBlockId().equals(DefaultAssets.DEFAULT_BLOCK_ID) && !(Boolean)SyncConfig.ENABLE_TABLE_FILTER.get()) {
               return null;
            }

            RecipeFilter filter = blockIndex.getFilter();
            return filter != null ? filter.filter(recipeIds, Pair::value) : null;
         }).orElse(recipeIds).forEach(entryx -> {
            Identifier groupNamex = (Identifier)entryx.key();
            if (recipeKeys.containsKey(groupNamex)) {
               recipes.computeIfAbsent(groupNamex, g -> Lists.newArrayList()).add((Identifier)entryx.value());
            }
         });

         for (Entry<Identifier, List<Identifier>> entry : recipes.entrySet()) {
            if (!entry.getValue().isEmpty()) {
               this.recipes.put(entry.getKey(), entry.getValue());
               this.recipeKeys.put(entry.getKey(), recipeKeys.get(entry.getKey()));
            }
         }

         if (!this.recipeKeys.containsKey(this.selectedType)) {
            this.selectedType = null;
            this.selectedRecipeList = null;
            this.indexPage = 0;
         }

         if (!this.recipeKeys.keySet().isEmpty() && this.selectedType == null) {
            this.selectedType = this.recipeKeys.keySet().iterator().next();
         }

         if (this.selectedType != null) {
            this.selectedRecipeList = this.recipes.get(this.selectedType);
         }
      }
   }

   private boolean isNameMatch(GunSmithTableRecipe recipe) {
      if (this.filterList != null && StringUtils.isNotBlank(this.filterList.getSearchText())) {
         String searchText = this.filterList.getSearchText().toLowerCase();
         Component name = recipe.getResult().getResult().getHoverName();
         return name.getString().toLowerCase().contains(searchText);
      } else {
         return true;
      }
   }

   private boolean isSuitableForMainHand(GunSmithTableRecipe recipe) {
      if (this.filterList != null && this.filterList.isByHandSelected()) {
         ItemStack result = recipe.getResult().getResult();
         Minecraft minecraft = Minecraft.getInstance();
         ItemStack stack = minecraft.player != null ? minecraft.player.getMainHandItem() : ItemStack.EMPTY;
         if (stack.getItem() instanceof IGun igun) {
            if (result.getItem() instanceof IAmmo iAmmo) {
               return iAmmo.isAmmoOfGun(stack, result);
            }

            if (result.getItem() instanceof IAttachment) {
               return igun.allowAttachment(stack, result);
            }

            return false;
         }

         if (stack.getItem() instanceof IAttachment) {
            if (result.getItem() instanceof IGun iGun) {
               return iGun.allowAttachment(result, stack);
            }

            return false;
         }

         if (stack.getItem() instanceof IAmmo iAmmo) {
            if (result.getItem() instanceof IGun) {
               return iAmmo.isAmmoOfGun(result, stack);
            }

            return false;
         }
      }

      return true;
   }

   private boolean shouldFilterByMainHand() {
      Minecraft minecraft = Minecraft.getInstance();
      ItemStack stack = minecraft.player != null ? minecraft.player.getMainHandItem() : ItemStack.EMPTY;
      Item item = stack.getItem();
      return item instanceof IGun || item instanceof IAttachment || item instanceof IAmmo;
   }

   private void updateSelectedRecipeAfterFiltering() {
      if (this.selectedRecipeList != null && !this.selectedRecipeList.isEmpty()) {
         boolean selectedRecipeExists = this.selectedRecipe != null && this.selectedRecipeList.contains(this.selectedRecipe.id());
         if (!selectedRecipeExists) {
            this.selectedRecipe = this.getSelectedRecipe(this.selectedRecipeList.get(0));
         }

         this.getPlayerIngredientCount(this.selectedRecipe);
      } else {
         this.selectedRecipe = null;
         this.playerIngredientCount = null;
      }
   }

   public void setIndexPage(int indexPage) {
      this.indexPage = indexPage;
   }

   @Nullable
   private com.tacz.guns.client.resource.ClientRecipeCache.Entry getSelectedRecipe(Identifier recipeId) {
      if (Minecraft.getInstance().level != null) {
         return com.tacz.guns.client.resource.ClientRecipeCache.entry(recipeId);
      }

      return null;
   }

   private void getPlayerIngredientCount(com.tacz.guns.client.resource.ClientRecipeCache.Entry holder) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && holder != null) {
         List<GunSmithTableIngredient> ingredients = ((GunSmithTableRecipe)holder.value()).getInputs();
         int size = ingredients.size();
         this.playerIngredientCount = new Int2IntArrayMap(size);

         for (int i = 0; i < size; i++) {
            GunSmithTableIngredient ingredient = ingredients.get(i);
            Inventory inventory = player.getInventory();
            int count = 0;

            for (ItemStack stack : inventory.getNonEquipmentItems()) {
               if (!stack.isEmpty() && ingredient.getIngredient().test(stack)) {
                  count += stack.getCount();
               }
            }

            this.playerIngredientCount.put(i, count);
         }
      }
   }

   public void updateIngredientCount() {
      if (this.selectedRecipe != null) {
         this.getPlayerIngredientCount(this.selectedRecipe);
      }

      this.init();
   }

   public void init() {
      super.init();
      if (this.filterList == null) {
         this.filterList = new GunPackList(
            this.minecraft, 134, this.imageHeight, this.topPos, this.topPos + this.imageHeight + 1, 15, this.recipes, this
         );
      }

      if (!this.autoByHandFilterApplied && (Boolean)RenderConfig.AUTO_SELECT_GUN_SMITH_TABLE_FILTER.get()) {
         this.filterList.setByHandSelected(this.shouldFilterByMainHand());
         this.autoByHandFilterApplied = true;
      }

      this.filterList.setSize(134, this.imageHeight);
      this.filterList.setPosition(this.leftPos, this.topPos);
      this.classifyRecipes();
      this.updateSelectedRecipeAfterFiltering();
      this.clearWidgets();
      this.addTypePageButtons();
      this.addTypeButtons();
      this.addIndexPageButtons();
      this.addIndexButtons();
      this.addRenderableWidget(new FlatColorButton(this.leftPos - 10, this.topPos, 9, 9, Component.literal("F"), b -> {
         this.filterEnabled = !this.filterEnabled;
         this.init();
      }).setTooltips("gui.tacz.gun_smith_table.filter"));
      if (this.filterEnabled) {
         this.addRenderableWidget(this.filterList);
      } else {
         this.addScaleButtons();
         this.addUrlButton();
      }

      this.addCraftButton();
   }

   private void addCraftButton() {
      this.addRenderableWidget(new ImageButton(this.leftPos + 289, this.topPos + 162, 48, 18, 138, 164, 18, TEXTURE, b -> {
         if (this.selectedRecipe != null && this.playerIngredientCount != null) {
            List<GunSmithTableIngredient> inputs = ((GunSmithTableRecipe)this.selectedRecipe.value()).getInputs();
            int size = inputs.size();

            for (int i = 0; i < size; i++) {
               if (i >= this.playerIngredientCount.size()) {
                  return;
               }

               int hasCount = this.playerIngredientCount.get(i);
               int needCount = inputs.get(i).getCount();
               boolean isCreative = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative();
               if (hasCount < needCount && !isCreative) {
                  return;
               }
            }

            ClientPlayNetworking.send(new ClientMessageCraft(this.selectedRecipe.id(), ((GunSmithTableMenu)this.menu).containerId));
         }
      }));
   }

   private void addUrlButton() {
      this.addRenderableWidget(new ImageButton(this.leftPos + 112, this.topPos + 164, 18, 18, 149, 211, 18, TEXTURE, b -> {
         if (this.selectedRecipe != null) {
            ItemStack output = ((GunSmithTableRecipe)this.selectedRecipe.value()).getOutput();
            Item item = output.getItem();
            Identifier id;
            if (item instanceof IGun iGun) {
               id = iGun.getGunId(output);
            } else if (item instanceof IAttachment iAttachment) {
               id = iAttachment.getAttachmentId(output);
            } else {
               if (!(item instanceof IAmmo iAmmo)) {
                  return;
               }

               id = iAmmo.getAmmoId(output);
            }

            PackInfo packInfo = ClientAssetsManager.INSTANCE.getPackInfo(id);
            if (packInfo == null) {
               return;
            }

            String url = packInfo.getUrl();
            if (StringUtils.isNotBlank(url) && this.minecraft != null) {
               this.minecraft.gui.setScreen(new ConfirmLinkScreen(yes -> {
                  if (yes) {
                     Util.getPlatform().openUri(url);
                  }

                  this.minecraft.gui.setScreen(this);
               }, url, false));
            }
         }
      }));
   }

   private void addIndexButtons() {
      if (this.selectedRecipeList != null && !this.selectedRecipeList.isEmpty()) {
         for (int i = 0; i < 6; i++) {
            int finalIndex = i + this.indexPage * 6;
            if (finalIndex >= this.selectedRecipeList.size()) {
               break;
            }

            int yOffset = this.topPos + 66 + 17 * i;
            Identifier recipeId = this.selectedRecipeList.get(finalIndex);
            com.tacz.guns.client.resource.ClientRecipeCache.Entry holder = this.getSelectedRecipe(recipeId);
            if (holder != null) {
               ResultButton button = (ResultButton)this.addRenderableWidget(
                  new ResultButton(this.leftPos + 144, yOffset, ((GunSmithTableRecipe)holder.value()).getOutput(), b -> {
                     this.selectedRecipe = holder;
                     this.getPlayerIngredientCount(this.selectedRecipe);
                     this.init();
                  })
               );
               if (this.selectedRecipe != null && holder.id().equals(this.selectedRecipe.id())) {
                  button.setSelected(true);
               }
            }
         }
      }
   }

   private void addTypeButtons() {
      List<TabConfig> list = Arrays.asList(this.recipeKeys.values().toArray(new TabConfig[0]));

      for (int i = 0; i < 7; i++) {
         int typeIndex = this.typePage * 7 + i;
         if (typeIndex >= this.recipes.size()) {
            return;
         }

         TabConfig tabConfig = list.get(typeIndex);
         Identifier type = tabConfig.id();
         int xOffset = this.leftPos + 157 + 24 * i;
         ItemStack icon = tabConfig.icon();
         TypeButton typeButton = new TypeButton(xOffset, this.topPos + 2, icon, b -> {
            this.selectedType = type;
            this.selectedRecipeList = this.recipes.get(type);
            this.indexPage = 0;
            this.selectedRecipe = this.getSelectedRecipe(this.selectedRecipeList.isEmpty() ? null : this.selectedRecipeList.get(0));
            this.getPlayerIngredientCount(this.selectedRecipe);
            this.init();
         });
         typeButton.setTooltip(Tooltip.create(tabConfig.getName(), tabConfig.getName()));
         if (this.selectedType.equals(type)) {
            typeButton.setSelected(true);
         }

         this.addRenderableWidget(typeButton);
      }
   }

   private void addIndexPageButtons() {
      this.addRenderableWidget(new ImageButton(this.leftPos + 143, this.topPos + 56, 96, 6, 40, 166, 6, TEXTURE, b -> {
         if (this.indexPage > 0) {
            this.indexPage--;
            this.init();
         }
      }));
      this.addRenderableWidget(new ImageButton(this.leftPos + 143, this.topPos + 171, 96, 6, 40, 186, 6, TEXTURE, b -> {
         if (this.selectedRecipeList != null && !this.selectedRecipeList.isEmpty()) {
            int maxIndexPage = (this.selectedRecipeList.size() - 1) / 6;
            if (this.indexPage < maxIndexPage) {
               this.indexPage++;
               this.init();
            }
         }
      }));
   }

   private void addTypePageButtons() {
      this.addRenderableWidget(new ImageButton(this.leftPos + 136, this.topPos + 4, 18, 20, 0, 162, 20, TEXTURE, b -> {
         if (this.typePage > 0) {
            this.typePage--;
            this.init();
         }
      }));
      this.addRenderableWidget(new ImageButton(this.leftPos + 327, this.topPos + 4, 18, 20, 20, 162, 20, TEXTURE, b -> {
         int maxIndexPage = (this.recipes.size() - 1) / 7;
         if (this.typePage < maxIndexPage) {
            this.typePage++;
            this.init();
         }
      }));
   }

   private void addScaleButtons() {
      this.addRenderableWidget(
         new ImageButton(this.leftPos + 5, this.topPos + 5, 10, 10, 188, 173, 10, TEXTURE, b -> this.scale = Math.min(this.scale + 20, 200))
      );
      this.addRenderableWidget(
         new ImageButton(this.leftPos + 17, this.topPos + 5, 10, 10, 200, 173, 10, TEXTURE, b -> this.scale = Math.max(this.scale - 20, 10))
      );
      this.addRenderableWidget(new ImageButton(this.leftPos + 29, this.topPos + 5, 10, 10, 212, 173, 10, TEXTURE, b -> this.scale = 70));
   }

   public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
      if (pMouseX > this.leftPos + 143 && pMouseX < this.leftPos + 143 + 94 && pMouseY > this.topPos + 66 && pMouseY < this.topPos + 66 + 85) {
         if (pScrollY > 0.0) {
            this.indexPage = Math.max(0, this.indexPage - 1);
         } else {
            int maxIndexPage = (this.selectedRecipeList.size() - 1) / 6;
            this.indexPage = Math.min(maxIndexPage, this.indexPage + 1);
         }

         this.init();
         return true;
      } else {
         return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
      }
   }

   /** The screen draws its own captions; the default container title would show through the buttons. */
   @Override
   protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
   }

   public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      drawModCenteredString(
         graphics, this.font, Component.translatable("gui.tacz.gun_smith_table.preview"), this.leftPos + 108, this.topPos + 5, 0xFF555555
      );
      if (this.selectedType != null) {
         TabConfig config = this.recipeKeys.get(this.selectedType);
         if (config != null) {
            graphics.text(this.font, config.getName(), this.leftPos + 150, this.topPos + 32, (0xFF000000 | 5592405), false);
         }
      }

      graphics.text(
         this.font, Component.translatable("gui.tacz.gun_smith_table.ingredient"), this.leftPos + 254, this.topPos + 50, (0xFF000000 | 5592405), false
      );
      drawModCenteredString(
         graphics, this.font, Component.translatable("gui.tacz.gun_smith_table.craft"), this.leftPos + 312, this.topPos + 167, 0xFFFFFFFF
      );
      if (!this.filterEnabled && this.selectedRecipe != null) {
         this.renderLeftModel((GunSmithTableRecipe)this.selectedRecipe.value(), graphics);
         this.renderPackInfo(graphics, this.selectedRecipe);
         graphics.text(
            this.font,
            Component.translatable(
               "gui.tacz.gun_smith_table.count", new Object[]{((GunSmithTableRecipe)this.selectedRecipe.value()).getResult().getResult().getCount()}
            ),
            this.leftPos + 254,
            this.topPos + 140,
            0xFF555555,
            false
         );
      }

      if (this.selectedRecipeList != null && !this.selectedRecipeList.isEmpty()) {
         this.renderIngredient(graphics);
      }

      ((ScreenAccessor)this)
         .tacz$getRenderables()
         .stream()
         .filter(w -> w instanceof ResultButton)
         .forEach(w -> ((ResultButton)w).renderTooltips(stack -> graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY)));
   }

   private void renderPackInfo(GuiGraphicsExtractor gui, com.tacz.guns.client.resource.ClientRecipeCache.Entry holder) {
      ItemStack output = ((GunSmithTableRecipe)holder.value()).getOutput();
      Item item = output.getItem();
      Identifier id;
      if (item instanceof IGun iGun) {
         id = iGun.getGunId(output);
      } else if (item instanceof IAttachment iAttachment) {
         id = iAttachment.getAttachmentId(output);
      } else {
         if (!(item instanceof IAmmo iAmmo)) {
            return;
         }

         id = iAmmo.getAmmoId(output);
      }

      PackInfo packInfo = ClientAssetsManager.INSTANCE.getPackInfo(id);
      org.joml.Matrix3x2fStack poseStack = gui.pose();
      if (packInfo != null) {
         poseStack.pushMatrix();
         poseStack.scale(0.75F, 0.75F);
         Component nameText = Component.translatable(packInfo.getName());
         gui.text(
            this.font, nameText, (int)((this.leftPos + 6) / 0.75F), (int)((this.topPos + 122) / 0.75F), 0xFF555555, false
         );
         poseStack.popMatrix();
         poseStack.pushMatrix();
         poseStack.scale(0.5F, 0.5F);
         int offsetX = (this.leftPos + 6) * 2;
         int offsetY = (this.topPos + 123) * 2;
         int nameWidth = this.font.width(nameText);
         Component ver = Component.literal("v" + packInfo.getVersion()).withStyle(ChatFormatting.UNDERLINE);
         gui.text(this.font, ver, (int)(offsetX + nameWidth * 0.75F / 0.5F + 5.0F), offsetY, 0xFF555555, false);
         offsetY += 14;
         String descKey = packInfo.getDescription();
         if (StringUtils.isNoneBlank(new CharSequence[]{descKey})) {
            Component desc = Component.translatable(descKey);

            for (FormattedCharSequence charSequence : this.font.split(desc, 245)) {
               gui.text(this.font, charSequence, offsetX, offsetY, 0xFF555555, false);
               offsetY += 9;
            }

            offsetY += 3;
         }

         gui.text(
            this.font,
            Component.translatable("gui.tacz.gun_smith_table.license")
               .append(Component.literal(packInfo.getLicense()).withStyle(ChatFormatting.DARK_GRAY)),
            offsetX,
            offsetY,
            0xFF555555,
            false
         );
         offsetY += 12;
         List<String> authors = packInfo.getAuthors();
         if (!authors.isEmpty()) {
            gui.text(
               this.font,
               Component.translatable("gui.tacz.gun_smith_table.authors")
                  .append(Component.literal(StringUtils.join(authors, ", ")).withStyle(ChatFormatting.DARK_GRAY)),
               offsetX,
               offsetY,
               0xFF555555,
               false
            );
            offsetY += 12;
         }

         gui.text(
            this.font,
            Component.translatable("gui.tacz.gun_smith_table.date")
               .append(Component.literal(packInfo.getDate()).withStyle(ChatFormatting.DARK_GRAY)),
            offsetX,
            offsetY,
            0xFF555555,
            false
         );
         poseStack.popMatrix();
      } else {
         Identifier recipeId = holder.id();
         gui.text(
            this.font,
            Component.translatable("gui.tacz.gun_smith_table.error").withStyle(ChatFormatting.DARK_RED),
            this.leftPos + 6,
            this.topPos + 122,
            (0xFF000000 | 11468800),
            false
         );
         gui.text(
            this.font,
            Component.translatable("gui.tacz.gun_smith_table.error.id", new Object[]{recipeId.toString()}).withStyle(ChatFormatting.DARK_RED),
            this.leftPos + 6,
            this.topPos + 134,
            0xFFFFFFFF,
            false
         );
         PackInfo errorPackInfo = ClientAssetsManager.INSTANCE.getPackInfo(id);
         if (errorPackInfo != null) {
            gui.text(
               this.font,
               Component.translatable(errorPackInfo.getName()).withStyle(ChatFormatting.DARK_RED),
               this.leftPos + 6,
               this.topPos + 146,
               11468800,
               false
            );
         }
      }
   }

   private void renderIngredient(GuiGraphicsExtractor gui) {
      if (this.selectedRecipe != null) {
         List<GunSmithTableIngredient> inputs = ((GunSmithTableRecipe)this.selectedRecipe.value()).getInputs();

         for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 2; j++) {
               int index = i * 2 + j;
               if (index >= inputs.size()) {
                  return;
               }

               int offsetX = this.leftPos + 254 + 45 * j;
               int offsetY = this.topPos + 62 + 17 * i;
               GunSmithTableIngredient smithTableIngredient = inputs.get(index);
               Ingredient ingredient = smithTableIngredient.getIngredient();
               ItemStack[] items = ingredient.items().map(holder -> new ItemStack(holder)).toArray(ItemStack[]::new);
               if (items.length == 0) {
                  continue;
               }

               int itemIndex = (int)(System.currentTimeMillis() / 1000L) % items.length;
               ItemStack item = items[itemIndex];
               gui.fakeItem(item, offsetX, offsetY);
               org.joml.Matrix3x2fStack poseStack = gui.pose();
               poseStack.pushMatrix();
               poseStack.scale(0.75F, 0.75F);
               int count = smithTableIngredient.getCount();
               if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
                  gui.text(this.font, String.format("%d/∞", count), (int)((offsetX + 17) / 0.75F), (int)((offsetY + 8) / 0.75F), (0xFF000000 | 16777215), false);
               } else {
                  int hasCount = 0;
                  if (this.playerIngredientCount != null && index < this.playerIngredientCount.size()) {
                     hasCount = this.playerIngredientCount.get(index);
                  }

                  int color = count <= hasCount ? 0xFFFFFFFF : 0xFFFF5555;
                  gui.text(this.font, String.format("%d/%d", count, hasCount), (int)((offsetX + 17) / 0.75F), (int)((offsetY + 8) / 0.75F), color, false);
               }

               poseStack.popMatrix();
            }
         }
      }
   }

   private void renderLeftModel(GunSmithTableRecipe recipe, GuiGraphicsExtractor gui) {
      RenderDistance.markGuiRenderTimestamp();
      float itemScale = this.scale / 16.0F;
      int centerX = this.leftPos + 60 + 8;
      int centerY = this.topPos + 50 + 8;
      org.joml.Matrix3x2fStack pose = gui.pose();
      pose.pushMatrix();
      pose.translate(centerX - 8.0F * itemScale, centerY - 8.0F * itemScale);
      pose.scale(itemScale, itemScale);
      gui.item(recipe.getOutput(), 0, 0);
      pose.popMatrix();
   }

   @Override
   public void extractBackground(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
      gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SIDE, this.leftPos, this.topPos, 0.0F, 0.0F, 134, 187, 256, 256);
      gui.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 136, this.topPos + 27, 0.0F, 0.0F, 208, 160, 256, 256);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
