package com.tacz.guns.api.entity;

public enum ShootResult {
   SUCCESS,
   UNKNOWN_FAIL,
   COOL_DOWN,
   NO_AMMO,
   NOT_DRAW,
   NOT_GUN,
   ID_NOT_EXIST,
   NEED_BOLT,
   IS_RELOADING,
   IS_DRAWING,
   IS_BOLTING,
   IS_MELEE,
   IS_SPRINTING,
   NETWORK_FAIL,
   FORGE_EVENT_CANCEL,
   OVERHEATED;
}
