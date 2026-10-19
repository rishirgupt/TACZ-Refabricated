package com.tacz.guns.event.ammo;

import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.TransparentBlock;

public class DestroyGlassBlock {
   public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
      Level level = event.getLevel();
      BlockState state = event.getState();
      BlockPos pos = event.getHitResult().getBlockPos();
      EntityKineticBullet ammo = event.getAmmo();
      Block stateBlock = state.getBlock();
      NoteBlockInstrument instrument = state.instrument();
      if ((Boolean)AmmoConfig.DESTROY_GLASS.get()
         && (
            stateBlock instanceof StainedGlassBlock
               || stateBlock instanceof TintedGlassBlock
               || stateBlock instanceof StainedGlassPaneBlock
               || stateBlock instanceof TransparentBlock && instrument.equals(NoteBlockInstrument.HAT)
               || stateBlock instanceof IronBarsBlock && instrument.equals(NoteBlockInstrument.HAT)
         )) {
         level.destroyBlock(pos, false, ammo.getOwner());
      }
   }
}
