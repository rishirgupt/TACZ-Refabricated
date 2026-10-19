package com.tacz.guns.item;

import com.tacz.guns.entity.TargetMinecart;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class TargetMinecartItem extends Item {
   public TargetMinecartItem() {
      super(new Properties().stacksTo(1));
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos blockpos = context.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (!blockstate.is(BlockTags.RAILS)) {
         return InteractionResult.FAIL;
      }

      ItemStack itemstack = context.getItemInHand();
      if (!level.isClientSide()) {
         RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock baseRailBlock
            ? (RailShape)blockstate.getValue(baseRailBlock.getShapeProperty())
            : RailShape.NORTH_SOUTH;
         double yOffset = 0.0;
         if (railshape.isSlope()) {
            yOffset = 0.5;
         }

         TargetMinecart targetMinecart = new TargetMinecart(
            level, blockpos.getX() + 0.5, blockpos.getY() + 0.0625 + yOffset, blockpos.getZ() + 0.5
         );
         if (itemstack.has(DataComponents.CUSTOM_NAME)) {
            targetMinecart.setCustomName(itemstack.getHoverName());
         }

         level.addFreshEntity(targetMinecart);
         level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
      }

      itemstack.shrink(1);
      return InteractionResult.SUCCESS;
   }
}
