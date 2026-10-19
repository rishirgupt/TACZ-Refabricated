package com.tacz.guns.block.entity;

import com.tacz.guns.block.StatueBlock;
import com.tacz.guns.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup.Provider;

public class StatueBlockEntity extends BlockEntity {
   public static final BlockEntityType<StatueBlockEntity> TYPE = new BlockEntityType<>(StatueBlockEntity::new, java.util.Set.of(ModBlocks.STATUE));
   private static final String ITEM_TAG = "Item";
   private ItemStack gunItem = ItemStack.EMPTY;

   public StatueBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(TYPE, pPos, pBlockState);
   }

   public static void clientTick(Level level, BlockPos blockPos, BlockState state, StatueBlockEntity statueBlockEntity) {
      if (level.getGameTime() % 100L == 0L && !statueBlockEntity.gunItem.isEmpty()) {
         Direction direction = (Direction)state.getValue(StatueBlock.FACING);
         double x = blockPos.getX() + direction.getStepX() * 0.75 + 0.5;
         double z = blockPos.getZ() + direction.getStepZ() * 0.75 + 0.5;
         double dx = -0.02 + level.getRandom().nextDouble() * 0.04;
         double dz = -0.02 + level.getRandom().nextDouble() * 0.04;
         double dy = -0.02 + level.getRandom().nextDouble() * 0.04;
         level.addParticle(ParticleTypes.END_ROD, x, blockPos.getY() + 2.25, z, dx, dy, dz);
      }
   }

   public ItemStack getGunItem() {
      return this.gunItem;
   }

   public void setGun(ItemStack stack) {
      this.dropItem();
      this.gunItem = stack.copy();
      if (this.level != null) {
         BlockState state = this.level.getBlockState(this.worldPosition);
         this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
      }

      this.setChanged();
   }

   public void dropItem() {
      if (!this.gunItem.isEmpty() && this.level != null) {
         Direction direction = (Direction)this.getBlockState().getValue(StatueBlock.FACING);
         Block.popResource(this.level, this.worldPosition.relative(direction).above(), this.gunItem);
         this.gunItem = ItemStack.EMPTY;
         if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
         }

         this.setChanged();
      }
   }

   @Override
   protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
      super.loadAdditional(input);
      this.gunItem = input.read("Item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
   }

   @Override
   protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
      super.saveAdditional(output);
      output.store("Item", ItemStack.OPTIONAL_CODEC, this.gunItem);
   }

   @Override
   public CompoundTag getUpdateTag(Provider provider) {
      return this.saveWithoutMetadata(provider);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
