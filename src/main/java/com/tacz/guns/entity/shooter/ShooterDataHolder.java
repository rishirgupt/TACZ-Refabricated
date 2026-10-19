package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaValue;

public class ShooterDataHolder {
   public long baseTimestamp = System.currentTimeMillis();
   public long shootTimestamp = -1L;
   public long lastShootTimestamp = -1L;
   public long meleeTimestamp = -1L;
   public int meleePrepTickCount = -1;
   public long drawTimestamp = -1L;
   public long boltTimestamp = -1L;
   public boolean isBolting = false;
   public float aimingProgress = 0.0F;
   public long aimingTimestamp = -1L;
   public boolean isAiming = false;
   public long reloadTimestamp = -1L;
   @Nonnull
   public ReloadState.StateType reloadStateType = ReloadState.StateType.NOT_RELOADING;
   @Nullable
   public Supplier<ItemStack> currentGunItem = null;
   public float currentPutAwayTimeS = 0.0F;
   public float sprintTimeS = 0.0F;
   public long sprintTimestamp = -1L;
   public double knockbackStrength = -1.0;
   public int shootCount = 0;
   public float chargeProgress = 0.0F;
   public boolean isCrawling = false;
   @Nullable
   public LuaValue scriptData = null;
   public long heatTimestamp = -1L;
   @Nullable
   public AttachmentCacheProperty cacheProperty = null;

   public void initialData() {
      this.shootTimestamp = -1L;
      this.meleeTimestamp = -1L;
      this.meleePrepTickCount = -1;
      this.isAiming = false;
      this.aimingProgress = 0.0F;
      this.reloadTimestamp = -1L;
      this.reloadStateType = ReloadState.StateType.NOT_RELOADING;
      this.sprintTimestamp = -1L;
      this.sprintTimeS = 0.0F;
      this.boltTimestamp = -1L;
      this.isBolting = false;
      this.shootCount = 0;
      this.chargeProgress = 0.0F;
      this.scriptData = null;
      this.heatTimestamp = -1L;
   }
}
