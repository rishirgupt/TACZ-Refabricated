package com.tacz.guns.item;

import cn.sh1rocu.tacz.api.extension.IItem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.BlockItemBuilder;
import com.tacz.guns.api.item.nbt.BlockItemDataAccessor;
import com.tacz.guns.client.renderer.item.GunSmithTableItemRenderer;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import com.tacz.guns.inventory.tooltip.BlockItemTooltip;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class GunSmithTableItem extends BlockItem implements BlockItemDataAccessor, IItem {
   public GunSmithTableItem(Block block) {
      super(block, new Properties().stacksTo(1));
   }

   public static NonNullList<ItemStack> fillItemCategory() {
      NonNullList<ItemStack> stacks = NonNullList.create();
      TimelessAPI.getAllCommonBlockIndex().forEach(blockIndex -> {
         ItemStack stack = BlockItemBuilder.create(blockIndex.getValue().getBlock()).setId(blockIndex.getKey()).build();
         stacks.add(stack);
      });
      return stacks;
   }

   @Nonnull
   @Environment(EnvType.CLIENT)
   public Component getName(@Nonnull ItemStack stack) {
      Identifier blockId = this.getBlockId(stack);
      Optional<ClientBlockIndex> blockIndex = TimelessAPI.getClientBlockIndex(blockId);
      return (Component)(blockIndex.isPresent() ? Component.translatable(blockIndex.get().getName()) : super.getName(stack));
   }

   @NotNull
   public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
      return Optional.of(new BlockItemTooltip(this.getBlockId(pStack)));
   }
}
