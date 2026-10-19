package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerSprint {
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;
   public static boolean stopSprint = false;

   public LocalPlayerSprint(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public boolean getProcessedSprintStatus(boolean sprinting) {
      IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.player);
      ReloadState.StateType reloadStateType = gunOperator.getSynReloadState().getStateType();
      return !gunOperator.getSynIsAiming() && (!reloadStateType.isReloading() || reloadStateType.isReloadFinishing()) && !stopSprint ? sprinting : false;
   }
}
