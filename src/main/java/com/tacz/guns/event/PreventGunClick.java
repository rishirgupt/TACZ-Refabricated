package com.tacz.guns.event;

import com.tacz.guns.api.item.IGun;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class PreventGunClick {
   public static InteractionResult onLeftClickBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
      ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
      return itemInHand.getItem() instanceof IGun ? InteractionResult.FAIL : InteractionResult.PASS;
   }
}
