package com.tacz.guns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import org.jetbrains.annotations.Nullable;

public class GunSmithTableBlockC extends AbstractGunSmithTableBlock {
   public static final MapCodec<GunSmithTableBlockC> CODEC = simpleCodec(properties -> new GunSmithTableBlockC());
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

   public GunSmithTableBlockC() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
            .setValue(HALF, DoubleBlockHalf.LOWER)
      );
   }

   protected MapCodec<GunSmithTableBlockC> codec() {
      return CODEC;
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, HALF});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getHorizontalDirection().getOpposite();
      BlockPos clickedPos = context.getClickedPos();
      BlockPos above = clickedPos.above();
      Level level = context.getLevel();
      return level.getBlockState(above).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(above)
         ? (BlockState)this.defaultBlockState().setValue(FACING, direction)
         : null;
   }

   @Override
   public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(world, pos, state, placer, stack);
      if (!world.isClientSide()) {
         BlockPos above = pos.above();
         world.setBlock(above, (BlockState)state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
         world.updateNeighborsAt(pos, Blocks.AIR, null);
         state.updateNeighbourShapes(world, pos, 3);
      }
   }

   @Override
   protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, net.minecraft.util.RandomSource random) {
      DoubleBlockHalf half = (DoubleBlockHalf)state.getValue(HALF);
      return facing.getAxis() == Axis.Y
            && (
               half.equals(DoubleBlockHalf.LOWER) && facing == Direction.UP
                  || half.equals(DoubleBlockHalf.UPPER) && facing == Direction.DOWN
            )
            && !facingState.is(this)
         ? Blocks.AIR.defaultBlockState()
         : state;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
      if (!level.isClientSide() && player.isCreative()) {
         DoubleBlockHalf half = (DoubleBlockHalf)blockState.getValue(HALF);
         if (half == DoubleBlockHalf.UPPER) {
            BlockPos blockpos = pos.below();
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
               level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
               level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
         }
      }

      return super.playerWillDestroy(level, pos, blockState, player);
   }

   @Override
   public boolean isRoot(BlockState blockState) {
      return ((DoubleBlockHalf)blockState.getValue(HALF)).equals(DoubleBlockHalf.LOWER);
   }

   @Override
   public BlockPos getRootPos(BlockPos pos, BlockState blockState) {
      return ((DoubleBlockHalf)blockState.getValue(HALF)).equals(DoubleBlockHalf.LOWER) ? pos : pos.below();
   }
}
