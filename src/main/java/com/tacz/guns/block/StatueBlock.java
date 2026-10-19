package com.tacz.guns.block;

import cn.sh1rocu.tacz.api.extension.IBlockExtension;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.init.ModBlocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;

public class StatueBlock extends BaseEntityBlock implements IBlockExtension {
   public static final MapCodec<StatueBlock> CODEC = simpleCodec(properties -> new StatueBlock());
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

   public StatueBlock() {
      super(Properties.of().sound(SoundType.STONE).strength(2.0F, 3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(HALF, DoubleBlockHalf.LOWER))
            .setValue(FACING, Direction.NORTH)
      );
   }

   protected MapCodec<StatueBlock> codec() {
      return CODEC;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return ((DoubleBlockHalf)state.getValue(HALF)).equals(DoubleBlockHalf.LOWER) && level.isClientSide()
         ? createTickerHelper(blockEntityType, ModBlocks.STATUE_BE, StatueBlockEntity::clientTick)
         : null;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{HALF, FACING});
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return pState.getValue(HALF) == DoubleBlockHalf.LOWER ? new StatueBlockEntity(pPos, pState) : null;
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState pState, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit
   ) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
         pos = pos.below();
      }

      if (level.getBlockEntity(pos) instanceof StatueBlockEntity statueBlockEntity) {
         if (stack.getItem() instanceof IGun) {
            statueBlockEntity.setGun(stack);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
         }

         if (stack.isEmpty()) {
            statueBlockEntity.dropItem();
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.CONSUME;
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

   @Override
   protected void affectNeighborsAfterRemoval(BlockState pState, net.minecraft.server.level.ServerLevel pLevel, BlockPos pPos, boolean pMovedByPiston) {
      if (pLevel.getBlockEntity(pPos) instanceof StatueBlockEntity statueBlockEntity) {
         statueBlockEntity.dropItem();
      }

      super.affectNeighborsAfterRemoval(pState, pLevel, pPos, pMovedByPiston);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   @Override
   public void tacz$onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
      IBlockExtension.super.tacz$onBlockExploded(state, level, pos, explosion);
   }
}
