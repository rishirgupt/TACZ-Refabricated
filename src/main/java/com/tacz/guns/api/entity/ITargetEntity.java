package com.tacz.guns.api.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

public interface ITargetEntity {
   void onProjectileHit(Entity var1, EntityHitResult var2, DamageSource var3, float var4);
}
