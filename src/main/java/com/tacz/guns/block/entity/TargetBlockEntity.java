package com.tacz.guns.block.entity;

import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.config.common.OtherConfig;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.init.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup.Provider;

public class TargetBlockEntity extends BlockEntity implements Nameable {
   public static final BlockEntityType<TargetBlockEntity> TYPE = new BlockEntityType<>(TargetBlockEntity::new, java.util.Set.of(ModBlocks.TARGET));
   private static final int RESET_TIME = 100;
   private static final String OWNER_TAG = "Owner";
   private static final String CUSTOM_NAME_TAG = "CustomName";
   public float rot = 0.0F;
   public float oRot = 0.0F;
   @Nullable
   private ResolvableProfile owner;
   @Nullable
   private Component name;

   public TargetBlockEntity(BlockPos pos, BlockState blockState) {
      super(TYPE, pos, blockState);
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, TargetBlockEntity pBlockEntity) {
      pBlockEntity.oRot = pBlockEntity.rot;
      if ((Boolean)state.getValue(TargetBlock.STAND)) {
         pBlockEntity.rot = Math.max(pBlockEntity.rot - 18.0F, 0.0F);
      } else {
         pBlockEntity.rot = Math.min(pBlockEntity.rot + 45.0F, 90.0F);
      }
   }

   @Nullable
   public ResolvableProfile getOwner() {
      return this.owner;
   }

   public void setOwner(@Nullable ResolvableProfile owner) {
      this.owner = owner;
      this.refresh();
   }

   @Override
   protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
      super.loadAdditional(input);
      this.owner = input.read("Owner", ResolvableProfile.CODEC).orElse(null);
      this.name = input.read("CustomName", net.minecraft.network.chat.ComponentSerialization.CODEC).orElse(null);
   }

   @Override
   protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
      super.saveAdditional(output);
      if (this.owner != null) {
         output.store("Owner", ResolvableProfile.CODEC, this.owner);
      }

      if (this.name != null) {
         output.store("CustomName", net.minecraft.network.chat.ComponentSerialization.CODEC, this.name);
      }
   }

   public Component getName() {
      return (Component)(this.name != null ? this.name : Component.empty());
   }

   @Nullable
   public Component getCustomName() {
      return this.name;
   }

   public void setCustomName(Component name) {
      this.name = name;
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag(Provider provider) {
      return this.saveWithoutMetadata(provider);
   }

   public void refresh() {
      this.setChanged();
      if (this.level != null) {
         BlockState state = this.level.getBlockState(this.worldPosition);
         this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
      }
   }

   public void hit(Level level, BlockState state, BlockHitResult hit, boolean isUpperBlock) {
      if (this.level != null && (Boolean)state.getValue(TargetBlock.STAND)) {
         BlockPos blockPos = hit.getBlockPos();
         if (isUpperBlock) {
            blockPos = blockPos.below();
            state = level.getBlockState(blockPos);
         }

         int redstoneStrength = TargetBlock.getRedstoneStrength(hit, isUpperBlock);
         level.setBlock(
            blockPos, (BlockState)((BlockState)state.setValue(TargetBlock.STAND, false)).setValue(TargetBlock.OUTPUT_POWER, redstoneStrength), 3
         );
         level.scheduleTick(blockPos, state.getBlock(), 100);
         float volume = ((Integer)OtherConfig.TARGET_SOUND_DISTANCE.get()).intValue() / 16.0F;
         volume = Math.max(volume, 0.0F);
         level.playSound(null, blockPos, ModSounds.TARGET_HIT, SoundSource.BLOCKS, volume, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
      }
   }
}
