package com.tacz.guns.block;

import com.tacz.guns.api.item.builder.BlockItemBuilder;
import com.tacz.guns.api.item.nbt.BlockItemDataAccessor;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractGunSmithTableBlock extends BaseEntityBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

   public AbstractGunSmithTableBlock() {
      super(Properties.of().sound(SoundType.WOOD).strength(2.0F, 3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected InteractionResult useWithoutItem(BlockState pState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (level.getBlockEntity(this.getRootPos(pos, pState)) instanceof GunSmithTableBlockEntity gunSmithTable && player instanceof ServerPlayer serverPlayer) {
         serverPlayer.openMenu(gunSmithTable);
      }

      return InteractionResult.CONSUME;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
      return new GunSmithTableBlockEntity(pos, blockState);
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.INVISIBLE;
   }

   public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(world, pos, state, placer, stack);
      if (!world.isClientSide() && stack.getItem() instanceof BlockItemDataAccessor accessor) {
         Identifier id = accessor.getBlockId(stack);
         if (world.getBlockEntity(pos) instanceof GunSmithTableBlockEntity e) {
            e.setId(id);
         }
      }
   }

   protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
      BlockPos blockPos = this.getRootPos(pos, state);
      if (level.getBlockEntity(blockPos) instanceof GunSmithTableBlockEntity e) {
         return e.getId() != null ? BlockItemBuilder.create(this).setId(e.getId()).build() : new ItemStack(this);
      } else {
         return super.getCloneItemStack(level, pos, state, includeData);
      }
   }

   public abstract boolean isRoot(BlockState var1);

   public float parseRotation(Direction direction) {
      return 90.0F * (3 - direction.get2DDataValue()) - 90.0F;
   }

   public abstract BlockPos getRootPos(BlockPos var1, BlockState var2);
}
