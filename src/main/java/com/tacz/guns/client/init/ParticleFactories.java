package com.tacz.guns.client.init;

import com.tacz.guns.client.particle.BulletHoleParticle;
import com.tacz.guns.init.ModParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


@Environment(EnvType.CLIENT)
public class ParticleFactories {
   public static void registerParticles() {
      net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry instance = net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance();
      instance.register(ModParticles.BULLET_HOLE, new BulletHoleParticle.Provider());
   }
}
