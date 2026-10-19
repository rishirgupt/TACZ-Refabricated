package com.tacz.guns.item;

import cn.sh1rocu.tacz.api.extension.IItem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import com.tacz.guns.client.renderer.item.AmmoItemRenderer;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import org.jetbrains.annotations.NotNull;

public class AmmoItem extends Item implements AmmoItemDataAccessor, IItem {
   public AmmoItem() {
      super(new Properties().stacksTo(1));
   }

   public void verifyComponentsAfterLoad(@NotNull ItemStack stack) {
      TimelessAPI.getCommonAmmoIndex(this.getAmmoId(stack))
         .map(CommonAmmoIndex::getStackSize)
         .ifPresent(maxStackSize -> stack.set(DataComponents.MAX_STACK_SIZE, maxStackSize));
   }

   @Nonnull
   @Environment(EnvType.CLIENT)
   public Component getName(@Nonnull ItemStack stack) {
      Identifier ammoId = this.getAmmoId(stack);
      Optional<ClientAmmoIndex> ammoIndex = TimelessAPI.getClientAmmoIndex(ammoId);
      return (Component)(ammoIndex.isPresent() ? Component.translatable(ammoIndex.get().getName()) : super.getName(stack));
   }

   private static Comparator<Entry<Identifier, CommonAmmoIndex>> idNameSort() {
      return Comparator.comparingInt(m -> m.getValue().getSort());
   }

   public static NonNullList<ItemStack> fillItemCategory() {
      NonNullList<ItemStack> stacks = NonNullList.create();
      TimelessAPI.getAllCommonAmmoIndex().stream().sorted(idNameSort()).forEach(entry -> {
         ItemStack itemStack = AmmoItemBuilder.create().setId(entry.getKey()).build();
         stacks.add(itemStack);
      });
      return stacks;
   }

   @Environment(EnvType.CLIENT)
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag isAdvanced) {
      Identifier ammoId = this.getAmmoId(stack);
      TimelessAPI.getClientAmmoIndex(ammoId).ifPresent(index -> {
         String tooltipKey = index.getTooltipKey();
         if (tooltipKey != null) {
            components.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
         }
      });
      PackInfo packInfoObject = ClientAssetsManager.INSTANCE.getPackInfo(ammoId);
      if (packInfoObject != null) {
         MutableComponent component = Component.translatable(packInfoObject.getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
         components.add(component);
      }
   }
}
