package com.tacz.guns.client.particle;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.particles.BulletHoleOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.LightCoordsUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BulletHoleParticle extends SingleQuadParticle {
   private final Direction direction;
   private final BlockPos pos;
   private int uOffset;
   private int vOffset;
   private float textureDensity;

   public BulletHoleParticle(
      ClientLevel world, double x, double y, double z, Direction direction, BlockPos pos, String ammoId, String gunId, String gunDisplayId
   ) {
      super(world, x, y, z, getSprite(pos));
      this.direction = direction;
      this.pos = pos;
      this.lifetime = this.getLifetimeFromConfig(world);
      this.hasPhysics = false;
      this.gravity = 0.0F;
      this.quadSize = 0.05F;
      BlockState state = world.getBlockState(pos);
      if (state.is(ModBlocks.TARGET) || this.shouldRemove()) {
         this.remove();
      }

      TimelessAPI.getGunDisplay(Identifier.parse(gunDisplayId), Identifier.parse(gunId)).ifPresent(gunIndex -> {
         float[] gunTracerColor = gunIndex.getTracerColor();
         if (gunTracerColor != null) {
            this.rCol = gunTracerColor[0];
            this.gCol = gunTracerColor[1];
            this.bCol = gunTracerColor[2];
         } else {
            TimelessAPI.getClientAmmoIndex(Identifier.parse(ammoId)).ifPresent(ammoIndex -> {
               float[] ammoTracerColor = ammoIndex.getTracerColor();
               this.rCol = ammoTracerColor[0];
               this.gCol = ammoTracerColor[1];
               this.bCol = ammoTracerColor[2];
            });
         }
      });
      this.alpha = 0.9F;
   }

   private int getLifetimeFromConfig(ClientLevel world) {
      int configLife = (Integer)RenderConfig.BULLET_HOLE_PARTICLE_LIFE.get();
      return configLife <= 1 ? configLife : configLife + world.getRandom().nextInt(configLife / 2);
   }

   protected void setSprite(TextureAtlasSprite sprite) {
      super.setSprite(sprite);
      this.uOffset = this.random.nextInt(16);
      this.vOffset = this.random.nextInt(16);
      this.textureDensity = (sprite.getU1() - sprite.getU0()) / 16.0F;
   }

   private static TextureAtlasSprite getSprite(BlockPos pos) {
      Minecraft minecraft = Minecraft.getInstance();
      Level world = minecraft.level;
      BlockState state = world != null ? world.getBlockState(pos) : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
      return minecraft.getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
   }

   protected float getU0() {
      return this.sprite.getU0() + this.uOffset * this.textureDensity;
   }

   protected float getV0() {
      return this.sprite.getV0() + this.vOffset * this.textureDensity;
   }

   protected float getU1() {
      return this.getU0() + this.textureDensity;
   }

   protected float getV1() {
      return this.getV0() + this.textureDensity;
   }

   public void tick() {
      super.tick();
      if (this.shouldRemove()) {
         this.remove();
      }
   }

   @Override
   public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
      Vec3 view = camera.position();
      Vec3i normal = this.direction.getUnitVec3i();
      float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - view.x()) + normal.getX() * 0.01F;
      float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - view.y()) + normal.getY() * 0.01F;
      float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - view.z()) + normal.getZ() * 0.01F;
      int light = Math.max(15 - this.age / 2, 0);
      float colorPercent = light / 15.0F;
      double threshold = (Double)RenderConfig.BULLET_HOLE_PARTICLE_FADE_THRESHOLD.get() * this.lifetime;
      float fade = 1.0F - (float)(Math.max(this.age - threshold, 0.0) / (this.lifetime - threshold));
      float savedAlpha = this.alpha;
      float savedR = this.rCol;
      float savedG = this.gCol;
      float savedB = this.bCol;
      this.alpha = savedAlpha * fade;
      this.rCol = savedR * colorPercent;
      this.gCol = savedG * colorPercent;
      this.bCol = savedB * colorPercent;
      Quaternionf rotation = new Quaternionf(this.direction.getRotation()).mul(new Quaternionf().rotationX((float)(-Math.PI / 2.0)));
      this.extractRotatedQuad(state, rotation, px, py, pz, partialTicks);
      this.alpha = savedAlpha;
      this.rCol = savedR;
      this.gCol = savedG;
      this.bCol = savedB;
   }

   @Override
   protected int getLightCoords(float partialTick) {
      int light = Math.max(15 - this.age / 2, 0);
      return LightCoordsUtil.pack(light, light);
   }

   @Override
   protected SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN;
   }

   private boolean shouldRemove() {
      BlockState blockState = this.level.getBlockState(this.pos);
      if (blockState.isAir()) {
         return true;
      }

      VoxelShape shape = blockState.getCollisionShape(this.level, this.pos);
      if (shape.isEmpty()) {
         return true;
      }

      AABB baseBlockBoundingBox = shape.bounds();
      AABB blockBoundingBox = baseBlockBoundingBox.move(this.pos);
      boolean intersects = blockBoundingBox.intersects(
         this.x - 0.1, this.y - 0.1, this.z - 0.1, this.x + 0.1, this.y + 0.1, this.z + 0.1
      );
      return !intersects;
   }

   @Environment(EnvType.CLIENT)
   public static class Provider implements ParticleProvider<BulletHoleOption> {
      public BulletHoleParticle createParticle(
         @NotNull BulletHoleOption option, @NotNull ClientLevel world, double x, double y, double z, double pXSpeed, double pYSpeed, double pZSpeed, net.minecraft.util.RandomSource random
      ) {
         return new BulletHoleParticle(world, x, y, z, option.getDirection(), option.getPos(), option.getAmmoId(), option.getGunId(), option.getGunDisplayId());
      }
   }
}
