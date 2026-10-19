package com.tacz.guns.block.entity;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.inventory.GunSmithTableMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.HolderLookup.Provider;
import org.jetbrains.annotations.Nullable;

public class GunSmithTableBlockEntity extends BlockEntity implements ExtendedMenuProvider<Identifier> {
   public static final BlockEntityType<GunSmithTableBlockEntity> TYPE = new BlockEntityType<>(GunSmithTableBlockEntity::new, java.util.Set.of(ModBlocks.GUN_SMITH_TABLE, ModBlocks.WORKBENCH_111, ModBlocks.WORKBENCH_121, ModBlocks.WORKBENCH_211));
   private static final String ID_TAG = "BlockId";
   @Nullable
   private Identifier id = null;

   public GunSmithTableBlockEntity(BlockPos pos, BlockState blockState) {
      super(TYPE, pos, blockState);
   }

   public void setId(Identifier id) {
      this.id = id;
   }

   @Nullable
   public Identifier getId() {
      return this.id;
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public Component getDisplayName() {
      return Component.literal("Gun Smith Table");
   }

   public Identifier getScreenOpeningData(ServerPlayer serverPlayer) {
      return this.getId() == null ? DefaultAssets.DEFAULT_BLOCK_ID : this.getId();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      return new GunSmithTableMenu(id, inventory, this.getId() == null ? DefaultAssets.DEFAULT_BLOCK_ID : this.getId());
   }

   @Override
   protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
      super.loadAdditional(input);
      this.id = input.getString("BlockId").map(Identifier::tryParse).orElse(DefaultAssets.DEFAULT_BLOCK_ID);
   }

   @Override
   protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
      super.saveAdditional(output);
      if (this.id != null) {
         output.putString("BlockId", this.id.toString());
      }
   }

   public CompoundTag getUpdateTag(Provider provider) {
      return this.saveWithoutMetadata(provider);
   }
}
