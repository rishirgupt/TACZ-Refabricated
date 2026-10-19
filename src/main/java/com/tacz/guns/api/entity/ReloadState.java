package com.tacz.guns.api.entity;

public class ReloadState {
   public static final int NOT_RELOADING_COUNTDOWN = -1;
   protected ReloadState.StateType stateType;
   protected long countDown;

   public ReloadState() {
      this.stateType = ReloadState.StateType.NOT_RELOADING;
      this.countDown = -1L;
   }

   public ReloadState(ReloadState src) {
      this.stateType = src.stateType;
      this.countDown = src.countDown;
   }

   public ReloadState.StateType getStateType() {
      return this.stateType;
   }

   public void setStateType(ReloadState.StateType stateType) {
      this.stateType = stateType;
   }

   public long getCountDown() {
      return this.stateType == ReloadState.StateType.NOT_RELOADING ? -1L : this.countDown;
   }

   public void setCountDown(long countDown) {
      this.countDown = countDown;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof ReloadState reloadState) ? false : reloadState.stateType.equals(this.stateType) && reloadState.countDown == this.countDown;
   }

   public enum StateType {
      NOT_RELOADING,
      EMPTY_RELOAD_FEEDING,
      EMPTY_RELOAD_FINISHING,
      TACTICAL_RELOAD_FEEDING,
      TACTICAL_RELOAD_FINISHING;

      public boolean isReloadingEmpty() {
         return this == EMPTY_RELOAD_FEEDING || this == EMPTY_RELOAD_FINISHING;
      }

      public boolean isReloadingTactical() {
         return this == TACTICAL_RELOAD_FEEDING || this == TACTICAL_RELOAD_FINISHING;
      }

      public boolean isReloading() {
         return this.isReloadingEmpty() || this.isReloadingTactical();
      }

      public boolean isReloadFinishing() {
         return this == EMPTY_RELOAD_FINISHING || this == TACTICAL_RELOAD_FINISHING;
      }
   }
}
