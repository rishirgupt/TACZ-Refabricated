package com.tacz.guns.block;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModBlocks;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;

public class TargetBlock extends BaseEntityBlock {
   public static final MapCodec<TargetBlock> CODEC = simpleCodec(properties -> new TargetBlock());
   public static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
   public static final BooleanProperty STAND = BooleanProperty.create("stand");
   public static final VoxelShape BOX_BOTTOM_STAND_X = Shapes.or(
      Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0), Block.box(6.0, 13.0, 2.0, 10.0, 16.0, 14.0)
   );
   public static final VoxelShape BOX_BOTTOM_STAND_Z = Shapes.or(
      Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0), Block.box(2.0, 13.0, 6.0, 14.0, 16.0, 10.0)
   );
   public static final VoxelShape BOX_BOTTOM_DOWN = Block.box(6.0, 0.0, 6.0, 10.0, 4.0, 10.0);
   public static final VoxelShape BOX_UPPER_X = Block.box(6.0, 0.0, 2.0, 10.0, 16.0, 14.0);
   public static final VoxelShape BOX_UPPER_Z = Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0);

   public TargetBlock() {
      super(Properties.of().sound(SoundType.WOOD).strength(2.0F, 3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                  .setValue(HALF, DoubleBlockHalf.LOWER))
               .setValue(STAND, true))
            .setValue(OUTPUT_POWER, 0)
      );
   }

   protected MapCodec<TargetBlock> codec() {
      return CODEC;
   }

   public static int getRedstoneStrength(BlockHitResult hit, boolean isUpperBlock) {
      if (!isUpperBlock) {
         return 1;
      }

      Vec3 hitLocation = hit.getLocation();
      Direction direction = hit.getDirection();
      double x = Math.abs(Mth.frac(hitLocation.x) - 0.5);
      double y = Math.abs(Mth.frac(hitLocation.y) - 0.32);
      double z = Math.abs(Mth.frac(hitLocation.z) - 0.5);
      Axis axis = direction.getAxis();
      double distance;
      if (axis == Axis.Y) {
         distance = Math.max(x, z);
      } else if (axis == Axis.Z) {
         distance = Math.max(x, y);
      } else {
         distance = Math.max(y, z);
      }

      double percent = Mth.clamp((0.25 - distance) / 0.25, 0.0, 1.0);
      return Math.max(1, Mth.ceil(15.0 * percent));
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return ((DoubleBlockHalf)state.getValue(HALF)).equals(DoubleBlockHalf.LOWER) && level.isClientSide()
         ? createTickerHelper(blockEntityType, ModBlocks.TARGET_BE, TargetBlockEntity::clientTick)
         : null;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, HALF, STAND, OUTPUT_POWER});
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
      return ((DoubleBlockHalf)blockState.getValue(HALF)).equals(DoubleBlockHalf.LOWER) ? new TargetBlockEntity(pos, blockState) : null;
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      boolean stand = (Boolean)state.getValue(STAND);
      boolean axis = ((Direction)state.getValue(FACING)).getAxis().equals(Axis.X);
      if (((DoubleBlockHalf)state.getValue(HALF)).equals(DoubleBlockHalf.UPPER)) {
         return stand ? (axis ? BOX_UPPER_X : BOX_UPPER_Z) : Shapes.empty();
      } else {
         return stand ? (axis ? BOX_BOTTOM_STAND_X : BOX_BOTTOM_STAND_Z) : BOX_BOTTOM_DOWN;
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!(Boolean)state.getValue(STAND)) {
         level.setBlock(pos, (BlockState)((BlockState)state.setValue(STAND, true)).setValue(OUTPUT_POWER, 0), 3);
      }
   }

   public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
      if (hit.getDirection().getOpposite().equals(state.getValue(FACING))) {
         if (((DoubleBlockHalf)state.getValue(HALF)).equals(DoubleBlockHalf.LOWER)) {
            world.getBlockEntity(hit.getBlockPos(), TargetBlockEntity.TYPE).ifPresent(e -> e.hit(world, state, hit, false));
         } else if (((DoubleBlockHalf)state.getValue(HALF)).equals(DoubleBlockHalf.UPPER)) {
            world.getBlockEntity(hit.getBlockPos().below(), TargetBlockEntity.TYPE).ifPresent(e -> e.hit(world, state, hit, true));
         }

         if (!world.isClientSide()
            && projectile.getOwner() instanceof Player player
            && (Boolean)state.getValue(STAND)
            && projectile instanceof EntityKineticBullet bullet) {
            String formattedDamage = String.format("%.1f", bullet.getDamage(hit.getLocation()));
            String formattedDistance = String.format("%.2f", hit.getLocation().distanceTo(player.position()));
            player.sendOverlayMessage(Component.translatable("message.tacz.target_minecart.hit", new Object[]{formattedDamage, formattedDistance}));
         }
      }
   }

   @Override
   protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, net.minecraft.util.RandomSource random) {
      DoubleBlockHalf half = (DoubleBlockHalf)state.getValue(HALF);
      boolean stand = (Boolean)state.getValue(STAND);
      if (facing.getAxis() == Axis.Y
         && (half.equals(DoubleBlockHalf.LOWER) && facing == Direction.UP || half.equals(DoubleBlockHalf.UPPER) && facing == Direction.DOWN)
         )
       {
         if (!facingState.is(this)) {
            return Blocks.AIR.defaultBlockState();
         }

         if ((Boolean)facingState.getValue(STAND) != stand) {
            return (BlockState)((BlockState)state.setValue(STAND, (Boolean)facingState.getValue(STAND)))
               .setValue(OUTPUT_POWER, (Integer)facingState.getValue(OUTPUT_POWER));
         }
      }

      return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos)
         ? Blocks.AIR.defaultBlockState()
         : state;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getHorizontalDirection();
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
         if (stack.has(DataComponents.CUSTOM_NAME) && world.getBlockEntity(pos) instanceof TargetBlockEntity e) {
            ResolvableProfile profile = ResolvableProfile.createUnresolved(stack.getHoverName().getString());
            e.setOwner(profile);
            e.setCustomName(stack.getHoverName());
            e.refresh();
         }
      }
   }

   protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
      BlockPos blockPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
      if (level.getBlockEntity(blockPos) instanceof TargetBlockEntity e) {
         ItemStack stack = new ItemStack(this);
         stack.set(DataComponents.CUSTOM_NAME, e.getCustomName());
         return stack;
      } else {
         return super.getCloneItemStack(level, pos, state, includeData);
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos blockpos = pos.below();
      BlockState blockstate = level.getBlockState(blockpos);
      return state.getValue(HALF) == DoubleBlockHalf.LOWER ? true : blockstate.is(this);
   }

   public boolean isSignalSource(BlockState state) {
      return true;
   }

   public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
      return (Integer)blockState.getValue(OUTPUT_POWER);
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (!level.isClientSide()
         && !state.is(oldState.getBlock())
         && (Integer)state.getValue(OUTPUT_POWER) > 0
         && !level.getBlockTicks().hasScheduledTick(pos, this)) {
         level.setBlock(pos, (BlockState)state.setValue(OUTPUT_POWER, 0), 18);
      }
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }
}
