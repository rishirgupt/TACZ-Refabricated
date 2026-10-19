package cn.sh1rocu.simplebedrockmodel.api.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public abstract class ViewportEvent extends BaseEvent {
   private final GameRenderer renderer;
   private final Camera camera;
   private final double partialTick;
   public static final Event<ViewportEvent.FovCallback> FOV = EventFactory.createArrayBacked(ViewportEvent.FovCallback.class, callbacks -> event -> {
      for (ViewportEvent.FovCallback callback : callbacks) {
         callback.post(event);
      }
   });
   public static final Event<ViewportEvent.CameraCallback> CAMERA = EventFactory.createWithPhases(
      ViewportEvent.CameraCallback.class, callbacks -> event -> {
         for (ViewportEvent.CameraCallback callback : callbacks) {
            callback.post(event);
         }
      }, new Identifier[]{Event.DEFAULT_PHASE}
   );

   public ViewportEvent(GameRenderer renderer, Camera camera, double partialTick) {
      this.renderer = renderer;
      this.camera = camera;
      this.partialTick = partialTick;
   }

   public GameRenderer getRenderer() {
      return this.renderer;
   }

   public Camera getCamera() {
      return this.camera;
   }

   public double getPartialTick() {
      return this.partialTick;
   }

   public interface CameraCallback {
      void post(ViewportEvent.ComputeCameraAngles event);
   }

   public static class ComputeCameraAngles extends ViewportEvent {
      private float yaw;
      private float pitch;
      private float roll;

      public ComputeCameraAngles(Camera camera, double renderPartialTicks, float yaw, float pitch, float roll) {
         super(Minecraft.getInstance().gameRenderer, camera, renderPartialTicks);
         this.yaw = yaw;
         this.pitch = pitch;
         this.roll = roll;
      }

      public float getYaw() {
         return this.yaw;
      }

      public void setYaw(float yaw) {
         this.yaw = yaw;
      }

      public float getPitch() {
         return this.pitch;
      }

      public void setPitch(float pitch) {
         this.pitch = pitch;
      }

      public float getRoll() {
         return this.roll;
      }

      public void setRoll(float roll) {
         this.roll = roll;
      }
   }

   public static class ComputeFov extends ViewportEvent {
      private final boolean usedConfiguredFov;
      private double fov;

      public ComputeFov(GameRenderer renderer, Camera camera, double renderPartialTicks, double fov, boolean usedConfiguredFov) {
         super(renderer, camera, renderPartialTicks);
         this.usedConfiguredFov = usedConfiguredFov;
         this.fov = fov;
      }

      public double getFOV() {
         return this.fov;
      }

      public void setFOV(double fov) {
         this.fov = fov;
      }

      public boolean usedConfiguredFov() {
         return this.usedConfiguredFov;
      }
   }

   public interface FovCallback {
      void post(ViewportEvent.ComputeFov event);
   }
}
