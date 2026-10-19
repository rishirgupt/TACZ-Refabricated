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
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import org.jetbrains.annotations.Nullable;

public class GunSmithTableBlockB extends AbstractGunSmithTableBlock {
   public static final MapCodec<GunSmithTableBlockB> CODEC = simpleCodec(properties -> new GunSmithTableBlockB());
   public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;

   public GunSmithTableBlockB() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
            .setValue(PART, BedPart.FOOT)
      );
   }

   protected MapCodec<GunSmithTableBlockB> codec() {
      return CODEC;
   }

   private static Direction getNeighbourDirection(BedPart bedPart, Direction direction) {
      return bedPart == BedPart.FOOT ? direction : direction.getOpposite();
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, PART});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getHorizontalDirection().getClockWise();
      BlockPos clickedPos = context.getClickedPos();
      BlockPos relative = clickedPos.relative(direction);
      Level level = context.getLevel();
      return level.getBlockState(relative).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(relative)
         ? (BlockState)this.defaultBlockState().setValue(FACING, direction)
         : null;
   }

   @Override
   public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(worldIn, pos, state, placer, stack);
      if (!worldIn.isClientSide()) {
         BlockPos relative = pos.relative((Direction)state.getValue(FACING));
         worldIn.setBlock(relative, (BlockState)state.setValue(PART, BedPart.HEAD), 3);
         worldIn.updateNeighborsAt(pos, Blocks.AIR, null);
         state.updateNeighbourShapes(worldIn, pos, 3);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
      if (!level.isClientSide() && player.isCreative()) {
         BedPart bedPart = (BedPart)blockState.getValue(PART);
         if (bedPart == BedPart.FOOT) {
            BlockPos blockpos = pos.relative(getNeighbourDirection(bedPart, (Direction)blockState.getValue(FACING)));
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(this) && blockstate.getValue(PART) == BedPart.HEAD) {
               level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
               level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
         }
      }

      return super.playerWillDestroy(level, pos, blockState, player);
   }

   @Override
   protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos facingPos, BlockState facingState, net.minecraft.util.RandomSource random) {
      if (direction != getNeighbourDirection((BedPart)state.getValue(PART), (Direction)state.getValue(FACING))) {
         return super.updateShape(state, level, tickAccess, currentPos, direction, facingPos, facingState, random);
      } else {
         return facingState.is(this) && facingState.getValue(PART) != state.getValue(PART) ? state : Blocks.AIR.defaultBlockState();
      }
   }

   @Override
   public boolean isRoot(BlockState blockState) {
      return ((BedPart)blockState.getValue(PART)).equals(BedPart.FOOT);
   }

   @Override
   public float parseRotation(Direction direction) {
      return 90 - 90 * direction.get2DDataValue();
   }

   @Override
   public BlockPos getRootPos(BlockPos pos, BlockState blockState) {
      return ((BedPart)blockState.getValue(PART)).equals(BedPart.FOOT)
         ? pos
         : pos.relative(getNeighbourDirection(BedPart.HEAD, (Direction)blockState.getValue(FACING)));
   }
}
