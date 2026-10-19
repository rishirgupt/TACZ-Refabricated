package com.tacz.guns.init;

import com.mojang.serialization.MapCodec;
import com.tacz.guns.particles.BulletHoleOption;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class ModParticles {
   public static final ParticleType<BulletHoleOption> BULLET_HOLE = register(
      "bullet_hole", new ModParticles.ModParticleType<>(false, BulletHoleOption.CODEC, BulletHoleOption.STREAM_CODEC)
   );

   public static void init() {
   }

   private static <T extends ParticleOptions> ParticleType<T> register(String name, ParticleType<T> type) {
      return (ParticleType<T>)Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("tacz", name), type);
   }

   private static class ModParticleType<T extends ParticleOptions> extends ParticleType<T> {
      private final MapCodec<T> codec;
      private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

      public ModParticleType(boolean overrideLimiter, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
         super(overrideLimiter);
         this.codec = codec;
         this.streamCodec = streamCodec;
      }

      @NotNull
      public MapCodec<T> codec() {
         return this.codec;
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
         return this.streamCodec;
      }
   }
}
